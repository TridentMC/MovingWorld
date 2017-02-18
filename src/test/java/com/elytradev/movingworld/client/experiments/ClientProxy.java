package com.elytradev.movingworld.client.experiments;

import com.elytradev.movingworld.client.experiments.render.MobileRegionRenderer;
import com.elytradev.movingworld.common.experiments.CommonProxy;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

/**
 * Created by darkevilmac on 2/16/2017.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenders() {
        RenderingRegistry.registerEntityRenderingHandler(EntityMobileRegion.class, MobileRegionRenderer::new);
    }
}
