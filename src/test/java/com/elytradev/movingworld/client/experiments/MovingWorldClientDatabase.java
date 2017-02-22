package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.common.experiments.IMovingWorldDB;
import com.google.common.collect.Maps;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

import java.util.HashMap;

/**
 * Created by darkevilmac on 2/21/2017.
 */
public class MovingWorldClientDatabase implements IMovingWorldDB {

    public HashMap<Integer, WorldClient> worlds = Maps.newHashMap();

    @Override
    public World getWorldFromDim(int dim) {
        return worlds.get(dim);
    }

    @Override
    public boolean addWorldForDim(int dim, World parent) {
        if (worlds.containsKey(dim))
            return false;

        WorldClient subWorld = new WorldClient(((WorldClient) parent).connection,
                new WorldSettings(0L, parent.getWorldInfo().getGameType(), false,
                        parent.getWorldInfo().isHardcoreModeEnabled(), parent.getWorldType()),
                dim, parent.getDifficulty(), new Profiler());

        worlds.put(dim, subWorld);

        return true;
    }

}
