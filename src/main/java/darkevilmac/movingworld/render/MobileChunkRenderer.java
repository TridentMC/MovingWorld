package darkevilmac.movingworld.render;

import com.sun.javafx.geom.Vec3d;
import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MobileChunkRenderer {
    /**
     * Should this renderer skip this render pass
     */
    public boolean[] skipRenderPass = new boolean[2];
    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    public boolean needsUpdate;
    public boolean isRemoved;
    /**
     * Axis aligned bounding box
     */
    public AxisAlignedBB rendererBoundingBox;
    public List<TileEntity> tileEntities;
    private MobileChunk chunk;
    private int glRenderList = 0;
    @SuppressWarnings("unused")
    private boolean isInitialized = false;
    /**
     * All the tile entities that have special rendering code for this chunk
     */
    private List<TileEntity> tileEntityRenderers = new ArrayList<TileEntity>();
    /**
     * Bytes sent to the GPU
     */
    @SuppressWarnings("unused")
    private int bytesDrawn;

    public MobileChunkRenderer(MobileChunk mobilechunk) {
        chunk = mobilechunk;
        needsUpdate = true;

        tileEntities = new ArrayList<TileEntity>();
    }


    public void render(double x, double y, double z, float partialTicks) {

        if (needsUpdate)
            updateSimpleRender(x, y, z);

    }

    private void updateSimpleRender(double renderX, double renderY, double renderZ) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(1.0F, 0.0F, 180.0F, 0.0F);
        GlStateManager.translate(0, 0, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

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
                    TileEntity tile = chunk.getTileEntity(pos);

                    if (tile != null && TileEntityRendererDispatcher.instance.hasSpecialRenderer(tile)) {
                        dispatchTileRender(tile, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                    }

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
        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    public void dispatchTileRender(TileEntity tileEntity, Vec3d worldPos) {
        // Renders a tile entity.
        TileEntityRendererDispatcher tesrDispatcher = TileEntityRendererDispatcher.instance;
        tesrDispatcher.renderTileEntityAt(tileEntity, worldPos.x, worldPos.y, worldPos.z, 0);
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
