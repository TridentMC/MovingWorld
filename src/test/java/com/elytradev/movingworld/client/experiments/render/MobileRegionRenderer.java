package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by darkevilmac on 2/16/2017.
 */
public class MobileRegionRenderer extends Render<EntityMobileRegion> {

    Map<EntityMobileRegion, RegionRenderer> regionRenderers = new HashMap<>();

    public MobileRegionRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityMobileRegion entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        BlockPos size = new BlockPos(entity.region.maxBlockPos()).subtract(entity.region.minBlockPos());

        System.out.println("EntityPos " + entity.getPosition().toString());

        float fx = size.getX() / 2;
        float fz = size.getZ() / 2;
        GlStateManager.translate(-fx, -0, -fz); //minY is always 0
        if (!regionRenderers.containsKey(entity))
            regionRenderers.put(entity, new RegionRenderer(entity));
        regionRenderers.get(entity).renderAll(partialTicks);
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
