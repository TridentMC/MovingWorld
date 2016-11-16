package io.github.elytra.movingworld.api;

import net.minecraft.util.math.BlockPos;

import io.github.elytra.movingworld.common.chunk.mobilechunk.MobileChunk;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;

public interface IMovingTile {

    void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos chunkPos);

    EntityMovingWorld getParentMovingWorld();

    void setParentMovingWorld(EntityMovingWorld entityMovingWorld);

    BlockPos getChunkPos();

    void setChunkPos(BlockPos chunkPos);

    /**
     * Called each tick from the mobilechunk, I advise strongly against any major modifications to
     * the chunk.
     */
    void tick(MobileChunk mobileChunk);

}
