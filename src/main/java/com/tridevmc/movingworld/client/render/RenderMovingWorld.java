package com.tridevmc.movingworld.client.render;

import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderMovingWorld extends Render<EntityMovingWorld> {
    public RenderMovingWorld(RenderManager renderManager) {
        super(renderManager);
        shadowSize = 1F;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityMovingWorld entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    public void renderVehicle(EntityMovingWorld entity, double x, double y, double z, float partialTicks) {
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

}
