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

package edu.uci.ics.hyracks.storage.am.btree.util;

import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparator;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.dataflow.common.util.SerdeUtils;
import edu.uci.ics.hyracks.storage.am.btree.frames.BTreeLeafFrameType;
import edu.uci.ics.hyracks.storage.am.btree.impls.BTree;
import edu.uci.ics.hyracks.storage.am.btree.tests.OrderedIndexTestContext;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndex;
import edu.uci.ics.hyracks.storage.common.buffercache.IBufferCache;

@SuppressWarnings("rawtypes")
public class BTreeTestContext extends OrderedIndexTestContext {
    
    public BTreeTestContext(ISerializerDeserializer[] fieldSerdes, ITreeIndex treeIndex) {
        super(fieldSerdes, treeIndex);
    }

    @Override
    public int getKeyFieldCount() {
        BTree btree = (BTree) treeIndex;
        return btree.getMultiComparator().getKeyFieldCount();
    }

    @Override
    public IBinaryComparator[] getComparators() {
        BTree btree = (BTree) treeIndex;
        return btree.getMultiComparator().getComparators();
    }
    
    public static BTreeTestContext create(IBufferCache bufferCache, int btreeFileId, ISerializerDeserializer[] fieldSerdes, int numKeyFields, BTreeLeafFrameType leafType) throws Exception {        
        ITypeTraits[] typeTraits = SerdeUtils.serdesToTypeTraits(fieldSerdes);
        IBinaryComparator[] cmps = SerdeUtils.serdesToComparators(fieldSerdes, numKeyFields);
        BTree btree = BTreeUtils.createBTree(bufferCache, btreeFileId, typeTraits, cmps, leafType);
        btree.create(btreeFileId);
        btree.open(btreeFileId);
        BTreeTestContext testCtx = new BTreeTestContext(fieldSerdes, btree);
        return testCtx;
    }
}
