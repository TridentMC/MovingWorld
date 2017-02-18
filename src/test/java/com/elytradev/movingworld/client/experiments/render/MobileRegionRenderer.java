package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Created by darkevilmac on 2/16/2017.
 */
public class MobileRegionRenderer extends Render<EntityMobileRegion> {

    public MobileRegionRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityMobileRegion entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        RegionRenderer regionRenderer = new RegionRenderer(entity);
        regionRenderer.renderAll(partialTicks);

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityMobileRegion entity) {
        return null;
    }

    @Override
    public boolean shouldRender(EntityMobileRegion livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true; // :)
    }
}
