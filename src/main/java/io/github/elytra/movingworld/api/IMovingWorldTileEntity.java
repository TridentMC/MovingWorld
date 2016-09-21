package io.github.elytra.movingworld.api;

import io.github.elytra.movingworld.common.chunk.mobilechunk.MobileChunk;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.util.math.BlockPos;

public interface IMovingWorldTileEntity {

    void setParentMovingWorld(BlockPos chunkPos, EntityMovingWorld entityMovingWorld);

    EntityMovingWorld getParentMovingWorld();

    void setParentMovingWorld(EntityMovingWorld entityMovingWorld);

    BlockPos getChunkPos();

    /**
     * Called each tick from the mobilechunk, I advise strongly against any major modifications to
     * the chunk.
     */
    void tick(MobileChunk mobileChunk);

}
