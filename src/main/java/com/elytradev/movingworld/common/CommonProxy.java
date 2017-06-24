package com.elytradev.movingworld.common;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CommonProxy {

    public World getWorld(int id){
        return DimensionManager.getWorld(id);
    }

}
