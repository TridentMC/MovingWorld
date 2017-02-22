package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.client.experiments.render.MobileRegionRenderer;
import com.elytradev.movingworld.common.experiments.CommonProxy;
import com.elytradev.movingworld.common.experiments.IMovingWorldDB;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraftforge.fml.client.registry.RenderingRegistry;


public class ClientProxy extends CommonProxy {

    private MovingWorldClientDatabase clientDatabase;

    @Override
    public void registerRenders() {
        RenderingRegistry.registerEntityRenderingHandler(EntityMobileRegion.class, MobileRegionRenderer::new);
    }

    @Override
    public void setupDB() {
        clientDatabase = new MovingWorldClientDatabase();
    }

    @Override
    public IMovingWorldDB getDB() {
        return clientDatabase;
    }
}
