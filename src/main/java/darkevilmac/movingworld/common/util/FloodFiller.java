package darkevilmac.movingworld.common.util;


import darkevilmac.movingworld.common.chunk.LocatedBlock;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.world.ChunkPosition;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Flood fill algorithm for finding if something is water tight.
 */

public class FloodFiller {

    private LocatedBlockList lbList = new LocatedBlockList();
    private ArrayList<ChunkPosition> posStack;

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
            ChunkPosition lbPos = lbIterator.next().coords;

            if (lbPos.chunkPosX > (mobileChunk.maxX() - 1) || lbPos.chunkPosX < mobileChunk.minX() ||
                    lbPos.chunkPosY > (mobileChunk.maxY() - 1) || lbPos.chunkPosY < mobileChunk.minY() ||
                    lbPos.chunkPosZ > (mobileChunk.maxZ() - 1) || lbPos.chunkPosZ < mobileChunk.minZ()
                    ) {
                lbIterator.remove();
            }
        }
    }

    private void fillCoord(MobileChunk mobileChunk, int startX, int startY, int startZ) {
        posStack = new ArrayList<ChunkPosition>();
        posStack.add(new ChunkPosition(startX, startY, startZ));

        while (!posStack.isEmpty()) {
            ChunkPosition pos = posStack.get(posStack.size() - 1);
            posStack.remove(posStack.size() - 1);

            Block block = mobileChunk.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            int blockMeta = mobileChunk.getBlockMetadata(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            int x = pos.chunkPosX;
            int y = pos.chunkPosY;
            int z = pos.chunkPosZ;

            if (block == null || block instanceof BlockAir || block.isAir(mobileChunk, x, y, z)) {
                if (x > mobileChunk.maxX() || x < mobileChunk.minX() - 1 ||
                        y > mobileChunk.maxY() + 1 || y < mobileChunk.minY() - 1 ||
                        z > mobileChunk.maxZ() || z < mobileChunk.minZ() - 1
                        ) {
                    continue;
                }

                if (lbList.containsLBOfPos(pos))
                    continue;

                lbList.add(new LocatedBlock(block, blockMeta, pos));

                posStack.add(ChunkPositionUtils.combine(pos, new ChunkPosition(1, 0, 0)));
                posStack.add(ChunkPositionUtils.combine(pos, new ChunkPosition(0, 1, 0)));
                posStack.add(ChunkPositionUtils.combine(pos, new ChunkPosition(0, 0, 1)));
                posStack.add(ChunkPositionUtils.combine(pos, new ChunkPosition(-1, 0, 0)));
                posStack.add(ChunkPositionUtils.combine(pos, new ChunkPosition(0, -1, 0)));
                posStack.add(ChunkPositionUtils.combine(pos, new ChunkPosition(0, 0, -1)));
            } else {
                continue;
            }
        }
    }

}
