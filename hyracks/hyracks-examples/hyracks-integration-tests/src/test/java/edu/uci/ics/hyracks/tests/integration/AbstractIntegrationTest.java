/*
 * Copyright 2009-2010 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.hyracks.tests.integration;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import edu.uci.ics.hyracks.api.client.HyracksConnection;
import edu.uci.ics.hyracks.api.client.IHyracksClientConnection;
import edu.uci.ics.hyracks.api.comm.IFrameTupleAccessor;
import edu.uci.ics.hyracks.api.dataflow.value.IResultSerializer;
import edu.uci.ics.hyracks.api.dataflow.value.IResultSerializerFactory;
import edu.uci.ics.hyracks.api.dataset.IHyracksDataset;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.api.job.JobFlag;
import edu.uci.ics.hyracks.api.job.JobId;
import edu.uci.ics.hyracks.api.job.JobSpecification;
import edu.uci.ics.hyracks.client.dataset.HyracksDataset;
import edu.uci.ics.hyracks.control.cc.ClusterControllerService;
import edu.uci.ics.hyracks.control.common.controllers.CCConfig;
import edu.uci.ics.hyracks.control.common.controllers.NCConfig;
import edu.uci.ics.hyracks.control.nc.NodeControllerService;

public abstract class AbstractIntegrationTest {
    private static final Logger LOGGER = Logger.getLogger(AbstractIntegrationTest.class.getName());

    public static final String NC1_ID = "nc1";
    public static final String NC2_ID = "nc2";

    private static ClusterControllerService cc;
    private static NodeControllerService nc1;
    private static NodeControllerService nc2;
    private static IHyracksClientConnection hcc;

    private final List<File> outputFiles;

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    public AbstractIntegrationTest() {
        outputFiles = new ArrayList<File>();
    }

    @BeforeClass
    public static void init() throws Exception {
        CCConfig ccConfig = new CCConfig();
        ccConfig.clientNetIpAddress = "127.0.0.1";
        ccConfig.clientNetPort = 39000;
        ccConfig.clusterNetIpAddress = "127.0.0.1";
        ccConfig.clusterNetPort = 39001;
        ccConfig.profileDumpPeriod = 10000;
        File outDir = new File("target/ClusterController");
        outDir.mkdirs();
        File ccRoot = File.createTempFile(AbstractIntegrationTest.class.getName(), ".data", outDir);
        ccRoot.delete();
        ccRoot.mkdir();
        ccConfig.ccRoot = ccRoot.getAbsolutePath();
        cc = new ClusterControllerService(ccConfig);
        cc.start();

        NCConfig ncConfig1 = new NCConfig();
        ncConfig1.ccHost = "localhost";
        ncConfig1.ccPort = 39001;
        ncConfig1.clusterNetIPAddress = "127.0.0.1";
        ncConfig1.dataIPAddress = "127.0.0.1";
        ncConfig1.datasetIPAddress = "127.0.0.1";
        ncConfig1.nodeId = NC1_ID;
        nc1 = new NodeControllerService(ncConfig1);
        nc1.start();

        NCConfig ncConfig2 = new NCConfig();
        ncConfig2.ccHost = "localhost";
        ncConfig2.ccPort = 39001;
        ncConfig2.clusterNetIPAddress = "127.0.0.1";
        ncConfig2.dataIPAddress = "127.0.0.1";
        ncConfig2.datasetIPAddress = "127.0.0.1";
        ncConfig2.nodeId = NC2_ID;
        nc2 = new NodeControllerService(ncConfig2);
        nc2.start();

        hcc = new HyracksConnection(ccConfig.clientNetIpAddress, ccConfig.clientNetPort);
        hcc.createApplication("test", null);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Starting CC in " + ccRoot.getAbsolutePath());
        }
    }

    @AfterClass
    public static void deinit() throws Exception {
        nc2.stop();
        nc1.stop();
        cc.stop();
    }

    protected void runTest(JobSpecification spec) throws Exception {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(spec.toJSON().toString(2));
        }
        JobId jobId = hcc.startJob("test", spec, EnumSet.of(JobFlag.PROFILE_RUNTIME));
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(jobId.toString());
        }

        // My code
        int nReaders = 5;
        DatasetClientContext datasetClientCtx = new DatasetClientContext(spec.getFrameSize());
        IHyracksDataset hyracksDataset = new HyracksDataset(hcc, datasetClientCtx, nReaders);

        hyracksDataset.open(jobId, spec.getResultSetIds().get(0));
        /*        FrameTupleAccessor frameTupleAccessor = new FrameTupleAccessor(datasetClientCtx.getFrameSize(),
                        recordDescriptor);

                ByteBuffer readBuffer = datasetClientCtx.allocateFrame();
                ByteBufferInputStream bbis = new ByteBufferInputStream();
                DataInputStream di = new DataInputStream(bbis);

                while (true) {
                    readBuffer.clear();
                    int size = hyracksDataset.read(readBuffer);
                    if (size <= 0) {
                        break;
                    }
                    try {
                        frameTupleAccessor.reset(readBuffer);
                        System.out.println("Tuple count: " + recordDescriptor);
                        for (int tIndex = 0; tIndex < frameTupleAccessor.getTupleCount(); tIndex++) {
                            int start = frameTupleAccessor.getTupleStartOffset(tIndex)
                                    + frameTupleAccessor.getFieldSlotsLength();
                            bbis.setByteBuffer(readBuffer, start);
                            Object[] record = new Object[recordDescriptor.getFieldCount()];
                            for (int i = 0; i < record.length; ++i) {
                                Object instance = recordDescriptor.getFields()[i].deserialize(di);
                                if (i == 0) {
                                    System.out.print(String.valueOf(instance));
                                } else {
                                    System.out.print(", " + String.valueOf(instance));
                                }
                            }
                            System.out.println();
                        }
                    } catch (IOException e) {
                        throw new HyracksDataException(e);
                    }
                }
                // End of my code
        */
        hcc.waitForCompletion(jobId);
        dumpOutputFiles();
    }

    private void dumpOutputFiles() {
        if (LOGGER.isLoggable(Level.INFO)) {
            for (File f : outputFiles) {
                if (f.exists() && f.isFile()) {
                    try {
                        LOGGER.info("Reading file: " + f.getAbsolutePath() + " in test: " + getClass().getName());
                        String data = FileUtils.readFileToString(f);
                        LOGGER.info(data);
                    } catch (IOException e) {
                        LOGGER.info("Error reading file: " + f.getAbsolutePath());
                        LOGGER.info(e.getMessage());
                    }
                }
            }
        }
    }

    protected File createTempFile() throws IOException {
        File tempFile = File.createTempFile(getClass().getName(), ".tmp", outputFolder.getRoot());
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Output file: " + tempFile.getAbsolutePath());
        }
        outputFiles.add(tempFile);
        return tempFile;
    }

    protected IResultSerializerFactory getResultSerializedAppenderFactory() {
        return new IResultSerializerFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public IResultSerializer createResultSerializer(final PrintStream printStream) {
                return new IResultSerializer() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void init() throws HyracksDataException {

                    }

                    @Override
                    public boolean appendTuple(IFrameTupleAccessor tAccess, int tIdx) throws HyracksDataException {
                        return true;
                    }
                };
            }
        };
    }
}