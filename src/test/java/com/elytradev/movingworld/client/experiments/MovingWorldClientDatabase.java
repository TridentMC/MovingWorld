package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.common.experiments.IMovingWorldDB;
import net.minecraft.world.World;

/**
 * Created by darkevilmac on 2/21/2017.
 */
public class MovingWorldClientDatabase implements IMovingWorldDB {
    @Override
    public World getWorldFromDim(int dim) {
        return null;
    }

    @Override
    public boolean addWorldForDim(int dim, World world) {
        return false;
    }
}
