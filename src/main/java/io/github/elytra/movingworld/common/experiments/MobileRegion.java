package io.github.elytra.movingworld.common.experiments;

import net.minecraft.util.math.ChunkPos;

public class MobileRegion {

    /**
     * The size of a chunk, 16x16
     */
    public static int chunkSize = 16;

    public ChunkPos regionMin;
    public ChunkPos regionMax;

    public MobileRegion(ChunkPos regionMin, ChunkPos regionMax) {
        this.regionMin = regionMin;
        this.regionMax = regionMax;
    }
}
