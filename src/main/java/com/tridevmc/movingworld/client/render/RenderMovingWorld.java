package com.tridevmc.movingworld.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class RenderMovingWorld extends EntityRenderer<EntityMovingWorld> {
    public RenderMovingWorld(EntityRendererManager renderManager) {
        super(renderManager);
        shadowSize = 1F;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityMovingWorld entity) {
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

        bindEntityTexture(entity);
        ((MobileChunkClient) entity.getMobileChunk()).getRenderer().render(partialTicks);
        GlStateManager.popMatrix();

        AxisAlignedBB boundingBox = entity.getBoundingBox();
        WorldRenderer.drawBoundingBox(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ, 1, 1, 1, 1);
    }

    @Override
    public void doRender(EntityMovingWorld entity, double x, double y, double z, float yaw, float partialTicks) {
        renderVehicle(entity, x, y, z, partialTicks);
        super.doRender(entity, x, y, z, yaw, partialTicks);
    }

    @Override
    public boolean shouldRender(EntityMovingWorld entity, ICamera camera, double camX, double camY, double camZ) {
        return entity.isInRangeToRender3d(camX, camY, camZ);
    }

    private void renderBoxCorners(EntityMovingWorld entity, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(0.0F, 1.0F, 0.0F, 0.75F);
        GlStateManager.disableTexture();
        GlStateManager.lineWidth(6.0F);

        entity.getRealBoundingBox().drawLines(new Vec3d(-entity.posX + x, -entity.posY + y, -entity.posZ + z));

        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

}
