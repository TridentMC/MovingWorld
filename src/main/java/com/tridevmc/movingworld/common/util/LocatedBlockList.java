package com.tridevmc.movingworld.common.util;

import com.google.common.collect.HashBiMap;
import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class LocatedBlockList extends ArrayList<LocatedBlock> {

    private HashBiMap<BlockPos, LocatedBlock> posMap;

    public LocatedBlockList() {
        super();
        posMap = HashBiMap.create();
    }

    public LocatedBlockList(int initialSize) {
        super(initialSize);
    }

    @Override
    public boolean add(LocatedBlock locatedBlock) {
        if (!posMap.containsKey(locatedBlock.pos))
            posMap.put(locatedBlock.pos, locatedBlock);
        return super.add(locatedBlock);
    }

    @Override
    public void add(int index, LocatedBlock locatedBlock) {
        if (!posMap.containsKey(locatedBlock.pos))
            posMap.put(locatedBlock.pos, locatedBlock);
        super.add(index, locatedBlock);
    }

    public ArrayList<LocatedBlockList> getSortedAssemblyBlocks() {
        ArrayList<LocatedBlockList> lbListList = new ArrayList<>();

        lbListList.add(getHighPriorityAssemblyBlocks());
        lbListList.add(getStandardPriorityAssemblyBlocks());
        lbListList.add(getLowPriorityAssemblyBlocks());

        return lbListList;
    }

    public ArrayList<LocatedBlockList> getSortedDisassemblyBlocks() {
        ArrayList<LocatedBlockList> lbListList = new ArrayList<>();

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
                if (MovingWorldMod.ASSEMBLY_CONFIG.highPriorityAssemblyBlocks.contains(lb.getBlock())) {
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
                if (MovingWorldMod.ASSEMBLY_CONFIG.highPriorityDisassemblyBlocks.contains(lb.getBlock())) {
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
                if (MovingWorldMod.ASSEMBLY_CONFIG.lowPriorityAssemblyBlocks.contains(lb.getBlock())) {
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
                if (MovingWorldMod.ASSEMBLY_CONFIG.lowPriorityDisassemblyBlocks.contains(lb.getBlock())) {
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
                if (!MovingWorldMod.ASSEMBLY_CONFIG.highPriorityAssemblyBlocks.contains(lb.getBlock())
                        && !MovingWorldMod.ASSEMBLY_CONFIG.lowPriorityAssemblyBlocks.contains(lb.getBlock())) {
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
                if (!MovingWorldMod.ASSEMBLY_CONFIG.highPriorityDisassemblyBlocks.contains(lb.getBlock())
                        && !MovingWorldMod.ASSEMBLY_CONFIG.lowPriorityDisassemblyBlocks.contains(lb.getBlock())) {
                    lbList.add(lb);
                }
            }
        }

        return lbList;
    }

    public LocatedBlock getLBOfPos(BlockPos pos) {
        return posMap.get(pos);
    }

    public boolean containsLBOfPos(BlockPos pos) {
        return posMap.containsKey(pos);
    }

}
