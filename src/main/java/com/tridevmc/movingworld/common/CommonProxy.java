package com.tridevmc.movingworld.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class CommonProxy {

    public World getWorld(int id) {
        MinecraftServer currentServer = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return DimensionManager.getWorld(currentServer, DimensionType.getById(id), false, false);
    }

}
