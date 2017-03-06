package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.common.experiments.PlayerInputHelper;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
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
        if (PlayerInputHelper.INSTANCE.currentBlock.getFirst() != null) {
            PlayerInputHelper inputHelper = PlayerInputHelper.INSTANCE;
            Entity player = Minecraft.getMinecraft().player;
            double pX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double pY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double pZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

            // Collect data to render bb of selected block.
            BlockPos selectedPos = inputHelper.currentBlock.getSecond();
            AxisAlignedBB bb = entity.getMobileRegionWorld().getBlockState(selectedPos)
                    .getSelectedBoundingBox(entity.getMobileRegionWorld(), selectedPos)
                    .expandXyz(0.002D).offset(-pX, -pY, -pZ);
            bb = entity.region.convertRegionBBToRealWorld(bb);

            // Actually render.
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            RenderGlobal.drawSelectionBoundingBox(bb,
                    0, 0, 0, 0.4F);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        BlockPos size = new BlockPos(RegionPool.regionSize << 4, 0, RegionPool.regionSize << 4);

        float fx = size.getX() / 2;
        float fz = size.getZ() / 2;
        GlStateManager.translate(-fx, -0, -fz); //minY is always 0
        if (!regionRenderers.containsKey(entity) || regionRenderers.get(entity).worldClient == null)
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
