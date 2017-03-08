package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.client.experiments.InputReader;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

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
        BlockPos size = new BlockPos(RegionPool.regionSize << 4, 0, RegionPool.regionSize << 4);

        float fx = size.getX() / 2;
        float fz = size.getZ() / 2;
        GlStateManager.translate(-fx, -0, -fz); //minY is always 0
        if (!regionRenderers.containsKey(entity) || regionRenderers.get(entity).worldClient == null)
            regionRenderers.put(entity, new RegionRenderer(entity));
        regionRenderers.get(entity).renderAll(partialTicks);
        GlStateManager.popMatrix();

        if (InputReader.INSTANCE.controller.currentHit.getFirst() != null) {
            drawSelectionBox(entity, Minecraft.getMinecraft().player, InputReader.INSTANCE.controller.currentHit.getSecond(), partialTicks);
        }
    }

    public void drawSelectionBox(EntityMobileRegion entityMobileRegion, EntityPlayer player, RayTraceResult movingObjectPositionIn, float partialTicks) {
        if (movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            double pX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double pY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double pZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

            // Collect data to render bb of selected block.
            BlockPos selectedPos = InputReader.INSTANCE.controller.currentHit.getSecond().getBlockPos();
            AxisAlignedBB bb = entityMobileRegion.getMobileRegionWorld().getBlockState(selectedPos)
                    .getSelectedBoundingBox(entityMobileRegion.getMobileRegionWorld(), selectedPos)
                    .expandXyz(0.002D).offset(-pX, -pY, -pZ);
            bb = entityMobileRegion.region.convertRegionBBToRealWorld(bb);

            // Actually render.
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();

            RenderGlobal.drawSelectionBoundingBox(bb,
                    0, 0, 0, 0.4F);

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
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
