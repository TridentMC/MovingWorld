package darkevilmac.movingworld.render;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MobileChunkRenderer {
    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    public boolean needsUpdate;
    public boolean isRemoved;

    private MobileChunk chunk;
    private int glRenderList = 0;

    /**
     * Bytes sent to the GPU
     */
    @SuppressWarnings("unused")
    private int bytesDrawn;

    public MobileChunkRenderer(MobileChunk mobilechunk) {
        chunk = mobilechunk;
        needsUpdate = true;
    }

    public void render(float partialTicks) {
        if (needsUpdate) {
            updateSimpleRender(partialTicks);
        }
    }

    private void updateSimpleRender(float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(1.0F, 0.0F, 180.0F, 0.0F);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        worldrenderer.startDrawingQuads();
        worldrenderer.setVertexFormat(DefaultVertexFormats.BLOCK);
        for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
            for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
                for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState blockState = chunk.getBlockState(pos);
                    Block block = blockState.getBlock();

                    for (EnumWorldBlockLayer enumWorldBlockLayer : EnumWorldBlockLayer.values()) {
                        if (!block.canRenderInLayer(enumWorldBlockLayer)) continue;
                        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(enumWorldBlockLayer);

                        if (block.getRenderType() != -1) {
                            dispatchBlockRender(blockState, pos, worldrenderer);
                        }
                    }
                }
            }
        }
        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();

        GlStateManager.pushMatrix();
        for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
            for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
                for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
                    BlockPos pos = new BlockPos(x, y, z);
                    TileEntity tile = chunk.getTileEntity(pos);
                    if (tile != null) {
                        if (TileEntityRendererDispatcher.instance.hasSpecialRenderer(tile)) {
                            TileEntity tileClone = tile;
                            tileClone.setWorldObj(chunk.getFakeWorld());
                            TileEntityRendererDispatcher.instance.renderTileEntityAt(tileClone, tileClone.getPos().getX(), tileClone.getPos().getY(), tileClone.getPos().getZ(), partialTicks);
                        }
                    }
                }
            }
        }
        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    public void dispatchBlockRender(IBlockState blockState, BlockPos blockPos, WorldRenderer worldRenderer) {
        worldRenderer.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockRendererDispatcher.renderBlock(blockState, blockPos, chunk, worldRenderer);
    }

    public void markDirty() {
        needsUpdate = true;
    }

    public void markRemoved() {
        isRemoved = true;

        try {
            if (glRenderList != 0) {
                MovingWorld.logger.debug("Deleting mobile chunk display list " + glRenderList);
                GLAllocation.deleteDisplayLists(glRenderList);
                glRenderList = 0;
            }
        } catch (Exception e) {
            MovingWorld.logger.error("Failed to destroy mobile chunk display list", e);
        }
    }
}
