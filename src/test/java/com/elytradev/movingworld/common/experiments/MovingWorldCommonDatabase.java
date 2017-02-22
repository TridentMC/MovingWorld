package com.elytradev.movingworld.common.experiments;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Essentially a stub database, forwards to the forge DimensionManager.
 */
public class MovingWorldCommonDatabase implements IMovingWorldDB {
    @Override
    public World getWorldFromDim(int dim) {
        return DimensionManager.getWorld(dim);
    }

    @Override
    public boolean addWorldForDim(int dim, World parent) {
        return false;
    }
}
