/*
 * Copyright 2009-2012 by The Regents of the University of California
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

package edu.uci.ics.hyracks.storage.am.lsm.rtree.impls;

import edu.uci.ics.hyracks.storage.am.btree.impls.BTree;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMComponent;
import edu.uci.ics.hyracks.storage.am.lsm.common.impls.LSMComponentState;
import edu.uci.ics.hyracks.storage.am.rtree.impls.RTree;

public class LSMRTreeComponent implements ILSMComponent {

    private final RTree rtree;
    private final BTree btree;

    public LSMRTreeComponent(RTree rtree, BTree btree) {
        this.rtree = rtree;
        this.btree = btree;
    }

    @Override
    public void activate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void threadEnter() {
        // TODO Auto-generated method stub

    }

    @Override
    public void threadExit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setState(LSMComponentState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public LSMComponentState getState() {
        // TODO Auto-generated method stub
        return null;
    }

    public RTree getRTree() {
        return rtree;
    }

    public BTree getBTree() {
        return btree;
    }
}