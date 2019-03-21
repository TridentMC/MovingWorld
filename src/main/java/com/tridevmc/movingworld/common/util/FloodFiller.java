package com.tridevmc.movingworld.common.util;

import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Flood fill algorithm for finding if something is water tight.
 */

public class FloodFiller {

    private LocatedBlockList lbList = new LocatedBlockList();

    /**
     * @return amount of blocks that were filled in.
     */
    public LocatedBlockList floodFillMobileChunk(MobileChunk mobileChunk) {
        lbList = new LocatedBlockList();

        fillCoord(mobileChunk, mobileChunk.maxX() / 2, mobileChunk.maxY() + 1, mobileChunk.maxZ() / 2); // We start just outside of the bounds, this is so we don't start filling inside a room or something.
        cleanList(mobileChunk); // Clean the list of any out of bounds stuff.

        return lbList;
    }

    /**
     * Cleans the list of all the blocks outside of bounds.
     */
    private void cleanList(MobileChunk mobileChunk) {
        Iterator<LocatedBlock> lbIterator = lbList.iterator();

        while (lbIterator.hasNext()) {
            BlockPos lbPos = lbIterator.next().pos;

            if (lbPos.getX() > (mobileChunk.maxX() - 1) || lbPos.getX() < mobileChunk.minX() ||
                    lbPos.getY() > (mobileChunk.maxY() - 1) || lbPos.getY() < mobileChunk.minY() ||
                    lbPos.getZ() > (mobileChunk.maxZ() - 1) || lbPos.getZ() < mobileChunk.minZ()
                    ) {
                lbIterator.remove();
            }
        }
    }

    private void fillCoord(MobileChunk mobileChunk, int startX, int startY, int startZ) {
        ArrayList<BlockPos> posStack = new ArrayList<>();
        posStack.add(new BlockPos(startX, startY, startZ));

        while (!posStack.isEmpty()) {
            BlockPos pos = posStack.get(posStack.size() - 1);
            posStack.remove(posStack.size() - 1);
            IBlockState state = mobileChunk.getBlockState(pos);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            if (state == null || state.getBlock() instanceof BlockAir) {
                if (x > mobileChunk.maxX() || x < mobileChunk.minX() - 1 ||
                        y > mobileChunk.maxY() + 1 || y < mobileChunk.minY() - 1 ||
                        z > mobileChunk.maxZ() || z < mobileChunk.minZ() - 1
                        ) {
                    continue;
                }

                if (lbList.containsLBOfPos(pos))
                    continue;

                lbList.add(new LocatedBlock(mobileChunk.getBlockState(pos), pos));

                posStack.add(pos.add(1, 0, 0));
                posStack.add(pos.add(0, 1, 0));
                posStack.add(pos.add(0, 0, 1));
                posStack.add(pos.add(-1, 0, 0));
                posStack.add(pos.add(0, -1, 0));
                posStack.add(pos.add(0, 0, -1));
            }
        }
    }

}
