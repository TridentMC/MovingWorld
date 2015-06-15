package darkevilmac.movingworld.render;

import darkevilmac.movingworld.chunk.mobilechunk.MobileChunkClient;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderMovingWorld extends Render {
    public RenderMovingWorld(RenderManager renderManager) {
        super(renderManager);
        shadowSize = 1F;
    }

    public void renderVehicle(EntityMovingWorld entity, double x, double y, double z, float yaw, float renderTime) {
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * renderTime;

        float rx = entity.frontDirection.getHorizontalIndex() == 1 ? -1f : entity.frontDirection.getHorizontalIndex() == 3 ? 1f : 0f;
        float rz = entity.frontDirection.getHorizontalIndex() == 0 ? 1f : entity.frontDirection.getHorizontalIndex() == 2 ? -1f : 0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(yaw, 0F, 1F, 0F);
        GlStateManager.rotate(pitch, rx, 0f, rz);

        float fx = entity.getMovingWorldChunk().getCenterX();
        float fz = entity.getMovingWorldChunk().getCenterZ();
        GlStateManager.translate(-fx, -entity.getMovingWorldChunk().minY(), -fz); //minY is always 0

        bindEntityTexture(entity);
        ((MobileChunkClient) entity.getMovingWorldChunk()).getRenderer().render(0F);
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float pitch) {
        renderVehicle((EntityMovingWorld) entity, x, y, z, yaw, pitch);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationBlocksTexture;
    }
}
