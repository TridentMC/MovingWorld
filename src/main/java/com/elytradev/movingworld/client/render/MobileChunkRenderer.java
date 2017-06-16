package com.elytradev.movingworld.client.render;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MobileChunkRenderer {
    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    public boolean needsUpdate;
    public boolean isRemoved;

    private int displayList;

    private MobileChunk chunk;

    public MobileChunkRenderer(MobileChunk mobilechunk) {
        chunk = mobilechunk;
        needsUpdate = true;
    }

    public void render(float partialTicks) {
        if (isRemoved) {
            GLAllocation.deleteDisplayLists(displayList);
            return;
        }

        try {
            if (needsUpdate)
                compileSimpleRender();

            GlStateManager.callList(this.displayList);

            renderTiles(partialTicks);
        } catch (Exception e) {
            MovingWorldMod.LOG.error("Exception when rendering a MobileChunk! ", e);
        }

    }

    private void compileSimpleRender() {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, 4864);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        HashMap<BlockRenderLayer, List<Tuple<BlockPos, IBlockState>>> blockRenderMap = Maps.newHashMap();
        for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
            blockRenderMap.put(blockRenderLayer, new ArrayList());
        }
        // Collect block states.
        for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
            for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
                for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState blockState = chunk.getBlockState(pos);
                    Block block = blockState.getBlock();

                    for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                        if (!block.canRenderInLayer(blockState, blockRenderLayer)
                                || blockState.getRenderType().equals(EnumBlockRenderType.INVISIBLE)) continue;

                        blockRenderMap.get(blockRenderLayer).add(new Tuple<>(pos, blockState));
                    }
                }
            }
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        // Actually render.
        for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
            for (Tuple<BlockPos, IBlockState> blockRenderData : blockRenderMap.get(blockRenderLayer)) {
                dispatchBlockRender(blockRenderData.getFirst(), blockRenderData.getSecond(), buffer);
            }
        }

        buffer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        RenderHelper.enableStandardItemLighting();

        GlStateManager.glEndList();
        this.needsUpdate = false;
    }

    private void renderTiles(float partialTicks) {
        GlStateManager.pushMatrix();
        World tesrDispatchWorld = TileEntityRendererDispatcher.instance.world;
        TileEntityRendererDispatcher.instance.setWorld(chunk.getFakeWorld());
        for (Map.Entry<BlockPos, TileEntity> blockPosTileEntityEntry : chunk.chunkTileEntityMap.entrySet()) {
            TileEntity tile = blockPosTileEntityEntry.getValue();
            tile.setWorld(chunk.getFakeWorld());
            TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);
            if (renderer != null && tile.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks);
            }
            tile.setWorld(chunk.world);
        }
        TileEntityRendererDispatcher.instance.setWorld(tesrDispatchWorld);
        GlStateManager.popMatrix();
    }

    public void dispatchBlockRender(BlockPos blockPos, IBlockState blockState, BufferBuilder buffer) {
        buffer.color(1.0F, 1.0F, 1.0F, 1.0F);
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockRendererDispatcher.renderBlock(blockState, blockPos, chunk.getFakeWorld(), buffer);
    }

    public void markDirty() {
        needsUpdate = true;
    }

    public void markRemoved() {
        isRemoved = true;
    }
}
