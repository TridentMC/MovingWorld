package com.elytradev.movingworld.client;

import com.elytradev.movingworld.client.experiments.MobileRegionRenderer;
import com.elytradev.movingworld.common.CommonProxy;
import com.elytradev.movingworld.common.config.MainConfig;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    public MainConfig syncedConfig;

    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityMobileRegion.class, manager -> new MobileRegionRenderer(manager));
    }
}
