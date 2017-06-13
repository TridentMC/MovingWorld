package com.elytradev.movingworld.common.experiments.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

/**
 * A world provider for all movingworlds, basically makes voids of nothingness.
 */
public class MovingWorldProvider extends WorldProvider {
    public IChunkGenerator createChunkGenerator() {
        return new ChunkProviderEmpty(this.world);
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.getById(getDimension());
    }
}
