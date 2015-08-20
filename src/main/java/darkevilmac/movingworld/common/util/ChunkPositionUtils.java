package darkevilmac.movingworld.common.util;

import net.minecraft.world.ChunkPosition;

public class ChunkPositionUtils {

    public static ChunkPosition combine(ChunkPosition pos1, ChunkPosition pos2) {
        return new ChunkPosition(pos1.chunkPosX + pos2.chunkPosX, pos1.chunkPosY + pos2.chunkPosY, pos1.chunkPosZ + pos2.chunkPosZ);
    }

}
