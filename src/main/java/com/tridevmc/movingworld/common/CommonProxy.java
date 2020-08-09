package com.tridevmc.movingworld.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class CommonProxy {

    public World getWorld(RegistryKey<World> dim) {
        MinecraftServer currentServer = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return currentServer.getWorld(dim);
    }

}
