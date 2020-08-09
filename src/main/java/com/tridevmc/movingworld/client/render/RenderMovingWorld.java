package com.tridevmc.movingworld.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class RenderMovingWorld extends EntityRenderer<EntityMovingWorld> {
    public RenderMovingWorld(EntityRendererManager renderManager) {
        super(renderManager);
        shadowSize = 1F;
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(EntityMovingWorld entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    public void renderVehicle(EntityMovingWorld entity, double x, double y, double z, float partialTicks) {
        renderBoxCorners(entity, x, y, z);

        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;

        float rx = entity.frontDirection.getHorizontalIndex() == 1 ? -1f : entity.frontDirection.getHorizontalIndex() == 3 ? 1f : 0f;
        float rz = entity.frontDirection.getHorizontalIndex() == 0 ? 1f : entity.frontDirection.getHorizontalIndex() == 2 ? -1f : 0f;

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.rotatef(yaw, 0F, 1F, 0F);
        GlStateManager.rotatef(pitch, rx, 0f, rz);

        float fx = entity.getMobileChunk().getCenterX();
        float fz = entity.getMobileChunk().getCenterZ();
        GlStateManager.translated(-fx, -entity.getMobileChunk().minY(), -fz); //minY is always 0

        ((MobileChunkClient) entity.getMobileChunk()).getRenderer().render(partialTicks);
        GlStateManager.popMatrix();
    }

    @Override
    public void render(EntityMovingWorld entityIn, float entityYaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLightIn) {
        renderVehicle(entityIn, x, y, z, partialTicks);
        super.render(entityIn, entityYaw, partialTicks, matrix, buffer, packedLightIn);
    }

    @Override
    public boolean shouldRender(EntityMovingWorld entity, ClippingHelper camera, double camX, double camY, double camZ) {
        return entity.isInRangeToRender3d(camX, camY, camZ);
    }

    private void renderBoxCorners(EntityMovingWorld entity, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
        GlStateManager.color4f(0.0F, 1.0F, 0.0F, 0.75F);
        GlStateManager.disableTexture();
        GlStateManager.lineWidth(6.0F);

        //entity.getRealBoundingBox().drawLines(new Vector3d(-entity.posX + x, -entity.posY + y, -entity.posZ + z));

        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

}
