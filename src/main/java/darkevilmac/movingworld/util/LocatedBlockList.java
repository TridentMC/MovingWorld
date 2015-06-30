package darkevilmac.movingworld.util;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.LocatedBlock;

import java.util.ArrayList;

public class LocatedBlockList extends ArrayList<LocatedBlock> {

    public LocatedBlockList() {
        super();
    }

    public LocatedBlockList(int initialSize) {
        super(initialSize);
    }

    public ArrayList<LocatedBlockList> getSortedAssemblyBlocks() {
        ArrayList<LocatedBlockList> lbListList = new ArrayList<LocatedBlockList>();

        lbListList.add(getHighPriorityAssemblyBlocks());
        lbListList.add(getStandardPriorityAssemblyBlocks());
        lbListList.add(getLowPriorityAssemblyBlocks());

        return lbListList;
    }

    public ArrayList<LocatedBlockList> getSortedDisassemblyBlocks() {
        ArrayList<LocatedBlockList> lbListList = new ArrayList<LocatedBlockList>();

        lbListList.add(getHighPriorityDisassemblyBlocks());
        lbListList.add(getStandardPriorityDisassemblyBlocks());
        lbListList.add(getLowPriorityDisassemblyBlocks());

        return lbListList;
    }

    public LocatedBlockList getHighPriorityAssemblyBlocks() {
        LocatedBlockList thisList = new LocatedBlockList();
        thisList.addAll(this);
        LocatedBlockList lbList = new LocatedBlockList();

        if (!thisList.isEmpty()) {
            for (LocatedBlock lb : thisList) {
                if (MovingWorld.instance.mConfig.assemblePriorityConfig.getHighPriorityAssembly().contains(lb.getBlockName())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

    public LocatedBlockList getHighPriorityDisassemblyBlocks() {
        LocatedBlockList thisList = new LocatedBlockList();
        thisList.addAll(this);
        LocatedBlockList lbList = new LocatedBlockList();

        if (!thisList.isEmpty()) {
            for (LocatedBlock lb : thisList) {
                if (MovingWorld.instance.mConfig.assemblePriorityConfig.getHighPriorityDisassembly().contains(lb.getBlockName())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

    public LocatedBlockList getLowPriorityAssemblyBlocks() {
        LocatedBlockList thisList = new LocatedBlockList();
        thisList.addAll(this);
        LocatedBlockList lbList = new LocatedBlockList();

        if (!thisList.isEmpty()) {
            for (LocatedBlock lb : thisList) {
                if (MovingWorld.instance.mConfig.assemblePriorityConfig.getLowPriorityAssembly().contains(lb.getBlockName())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

    public LocatedBlockList getLowPriorityDisassemblyBlocks() {
        LocatedBlockList thisList = new LocatedBlockList();
        thisList.addAll(this);
        LocatedBlockList lbList = new LocatedBlockList();

        if (!thisList.isEmpty()) {
            for (LocatedBlock lb : thisList) {
                if (MovingWorld.instance.mConfig.assemblePriorityConfig.getLowPriorityDisassembly().contains(lb.getBlockName())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

    public LocatedBlockList getStandardPriorityAssemblyBlocks() {
        LocatedBlockList thisList = new LocatedBlockList();
        thisList.addAll(this);
        LocatedBlockList lbList = new LocatedBlockList();

        if (!thisList.isEmpty()) {
            for (LocatedBlock lb : thisList) {
                if (!MovingWorld.instance.mConfig.assemblePriorityConfig.getHighPriorityAssembly().contains(lb.getBlockName())
                        && !MovingWorld.instance.mConfig.assemblePriorityConfig.getLowPriorityAssembly().contains(lb.getBlockName())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

    public LocatedBlockList getStandardPriorityDisassemblyBlocks() {
        LocatedBlockList thisList = new LocatedBlockList();
        thisList.addAll(this);
        LocatedBlockList lbList = new LocatedBlockList();

        if (!thisList.isEmpty()) {
            for (LocatedBlock lb : thisList) {
                if (!MovingWorld.instance.mConfig.assemblePriorityConfig.getHighPriorityDisassembly().contains(lb.getBlockName())
                        && !MovingWorld.instance.mConfig.assemblePriorityConfig.getLowPriorityDisassembly().contains(lb.getBlockName())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

}
