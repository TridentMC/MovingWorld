package com.elytradev.movingworld.common.experiments;

import net.minecraft.world.World;

/**
 * Created by darkevilmac on 2/21/2017.
 */
public class MovingWorldCommonDatabase implements IMovingWorldDB {
    @Override
    public World getWorldFromDim(int dim) {
        return null;
    }

    @Override
    public boolean addWorldForDim(int dim, World world) {
        return false;
    }
}
