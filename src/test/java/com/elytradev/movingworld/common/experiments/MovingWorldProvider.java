package com.elytradev.movingworld.common.experiments;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;

/**
 * A world provider for all movingworlds, basically makes voids of nothingness.
 */
public class MovingWorldProvider extends WorldProvider {
    @Override
    public DimensionType getDimensionType() {
        return DimensionType.getById(getDimension());
    }

    public IChunkGenerator createChunkGenerator() {
        return new ChunkProviderEmpty(this.world);
    }
}
