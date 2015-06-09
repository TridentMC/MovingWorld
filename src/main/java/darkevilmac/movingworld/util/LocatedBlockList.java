package darkevilmac.movingworld.util;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.LocatedBlock;
import net.minecraft.block.Block;

import java.util.ArrayList;

public class LocatedBlockList extends ArrayList<LocatedBlock> {

    public LocatedBlockList() {
        super();
    }

    public LocatedBlockList(int initialSize) {
        super(initialSize);
    }

    public LocatedBlockList getHighPriorityBlocks() {
        LocatedBlockList lbList = (LocatedBlockList) this.clone();

        if (!lbList.isEmpty()) {
            for (int index = 0; index < lbList.size(); index++) {
                LocatedBlock lb = lbList.get(index);
                if (lb != null) {
                    String lbName = Block.blockRegistry.getNameForObject(lb.blockState.getBlock()).toString();
                    if (!MovingWorld.instance.mConfig.highPriorityAssembly.contains(lbName)) {
                        lbList.remove(index);
                    }
                }
            }
        }

        return lbList;
    }

    public LocatedBlockList getNormalPriorityBlocks() {
        LocatedBlockList lbList = (LocatedBlockList) this.clone();

        if (!lbList.isEmpty()) {
            for (int index = 0; index < lbList.size(); index++) {
                LocatedBlock lb = lbList.get(index);
                if (lb != null) {
                    String lbName = Block.blockRegistry.getNameForObject(lb.blockState.getBlock()).toString();
                    if (MovingWorld.instance.mConfig.highPriorityAssembly.contains(lbName)) {
                        lbList.remove(index);
                    }
                }
            }
        }

        return lbList;
    }

}
