package com.elytradev.movingworld.client;

import com.elytradev.movingworld.common.CommonProxy;
import com.elytradev.movingworld.common.config.MainConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {

    public MainConfig syncedConfig;

    @Override
    public World getWorld(int id) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return Minecraft.getMinecraft().world;

        return super.getWorld(id);
    }
}
