package com.tridevmc.movingworld.client.render;

import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.ModList;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MobileChunkRenderer {
    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    public boolean needsUpdate;
    public boolean isRemoved;
    public LegacyRender legacyRender = new LegacyRender();
    public VBORender vboRender = new VBORender();
    public static boolean hasOptifine = ModList.get().isLoaded("optifine");
    private boolean usingVBOs = useVBO();
    private MobileChunkClient chunk;


    public MobileChunkRenderer(MobileChunk mobilechunk) {
        chunk = (MobileChunkClient) mobilechunk;
        needsUpdate = true;
    }

    public boolean useVBO() {
        return OpenGlHelper.useVbo() && !hasOptifine;
    }

    public void render(float partialTicks) {
        try {
            if (this.usingVBOs != useVBO()) {
                this.usingVBOs = useVBO();
                // Remove the old render.
                if (this.usingVBOs) {
                    this.legacyRender.remove();
                } else {
                    this.vboRender.remove();
                }
                this.needsUpdate = true;
            }

            if (isRemoved) {
                this.vboRender.remove();
                this.legacyRender.remove();

                return;
            }

            if (this.needsUpdate) {
                this.vboRender.compile();
                this.legacyRender.compile();
                this.needsUpdate = false;
            }

            if (this.usingVBOs) {
                this.vboRender.render();
            } else {
                this.legacyRender.render();
            }

            // Tiles always render in the same way.
            renderTiles(partialTicks);
        } catch (Exception e) {
            MovingWorldMod.LOG.error("Exception when rendering a MobileChunk! {}", e.getMessage());
            e.printStackTrace();

            try {
                Tessellator.getInstance().getBuffer().finishDrawing();
            } catch (Exception e2) {
            }
            return;
        }
    }

    private void renderTiles(float partialTicks) {
        GlStateManager.pushMatrix();
        TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;
        World tesrDispatchWorld = dispatcher.world;
        dispatcher.setWorld(chunk.getFakeWorld());
        for (Map.Entry<BlockPos, TileEntity> blockPosTileEntityEntry : chunk.normalTESRS.entrySet()) {
            TileEntity tile = blockPosTileEntityEntry.getValue();
            tile.setWorld(chunk.getFakeWorld());
            TileEntityRenderer renderer = dispatcher.getRenderer(tile);
            if (renderer != null && tile.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
                dispatcher.render(tile, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks);
            }
            tile.setWorld(chunk.world);
        }
        dispatcher.setWorld(tesrDispatchWorld);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        dispatcher.preDrawBatch();
        for (Map.Entry<BlockPos, TileEntity> blockPosTileEntityEntry : chunk.fastTESRS.entrySet()) {
            TileEntity tile = blockPosTileEntityEntry.getValue();
            tile.setWorld(chunk.getFakeWorld());
            TileEntityRenderer renderer = dispatcher.getRenderer(tile);
            if (renderer != null && tile.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
                dispatcher.render(tile, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks);
            }
            tile.setWorld(chunk.world);
        }
        dispatcher.drawBatch(0);
        GlStateManager.popMatrix();
    }

    public void dispatchBlockRender(BlockPos blockPos, IBlockState blockState, BufferBuilder buffer) {
        buffer.color(1.0F, 1.0F, 1.0F, 1.0F);
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        blockRendererDispatcher.renderBlock(blockState, blockPos, chunk.getFakeWorld(), buffer, this.chunk.world.rand);
    }

    public void markDirty() {
        needsUpdate = true;
    }

    public void markRemoved() {
        isRemoved = true;
    }

    public class LegacyRender {

        private int displayList = -1;

        public void compile() {
            this.displayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.newList(this.displayList, 4864);

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
                blockRenderMap.put(blockRenderLayer, new ArrayList<>());
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
                    dispatchBlockRender(blockRenderData.getA(), blockRenderData.getB(), buffer);
                }
            }

            buffer.setTranslation(0.0D, 0.0D, 0.0D);
            tessellator.draw();
            GlStateManager.disableBlend();
            GlStateManager.disableCull();
            RenderHelper.enableStandardItemLighting();

            GlStateManager.endList();
        }

        public void remove() {
            if (displayList >= 0)
                GlStateManager.deleteLists(displayList, 1);
        }

        public void render() {
            // Just a sanity check so we don't call nothing and cause bad things to happen.
            if (displayList >= 0)
                GlStateManager.callList(this.displayList);
        }

    }

    public class VBORender {
        private final VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];

        public void compile() {
            remove();

            HashMap<BlockRenderLayer, List<Tuple<BlockPos, IBlockState>>> blockRenderMap = Maps.newHashMap();
            for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                blockRenderMap.put(blockRenderLayer, new ArrayList<>());
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

            for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                BlockRenderLayer renderLayer = BlockRenderLayer.values()[i];
                this.vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);

                List<Tuple<BlockPos, IBlockState>> data = blockRenderMap.get(renderLayer);
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

                BlockRendererDispatcher blockDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
                for (Tuple<BlockPos, IBlockState> datum : data) {
                    bufferBuilder.color(1.0F, 1.0F, 1.0F, 1.0F);
                    blockDispatcher.renderBlock(datum.getB(), datum.getA(), chunk.getFakeWorld(), bufferBuilder, chunk.getFakeWorld().getRandom());
                }
                bufferBuilder.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                        (float) TileEntityRendererDispatcher.staticPlayerY,
                        (float) TileEntityRendererDispatcher.staticPlayerZ);
                bufferBuilder.finishDrawing();
                vertexBuffers[i].bufferData(bufferBuilder.getByteBuffer());
                bufferBuilder.reset();
            }
        }

        private void renderLayer(BlockRenderLayer layer) {
            VertexBuffer vbo = vertexBuffers[layer.ordinal()];
            if (vbo == null)
                return;

            GlStateManager.pushMatrix();

            GlStateManager.enableClientState(32884);
            OpenGlHelper.glClientActiveTexture(getDefaultTexUnit());
            GlStateManager.enableClientState(32888);
            OpenGlHelper.glClientActiveTexture(getLightmapTexUnit());
            GlStateManager.enableClientState(32888);
            OpenGlHelper.glClientActiveTexture(getDefaultTexUnit());
            GlStateManager.enableClientState(32886);

            GlStateManager.pushMatrix();
            vbo.bindBuffer();
            setupArrayPointers();
            vbo.drawArrays(7);
            GlStateManager.popMatrix();
            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            GlStateManager.resetColor();

            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();

                switch (vertexformatelement$enumusage) {
                    case POSITION:
                        GlStateManager.disableClientState(32884);
                        break;
                    case UV:
                        OpenGlHelper.glClientActiveTexture(getDefaultTexUnit() + i);
                        GlStateManager.disableClientState(32888);
                        OpenGlHelper.glClientActiveTexture(getDefaultTexUnit());
                        break;
                    case COLOR:
                        GlStateManager.disableClientState(32886);
                        GlStateManager.resetColor();
                }
            }
            GlStateManager.popMatrix();
        }

        private void setupArrayPointers() {
            GlStateManager.vertexPointer(3, 5126, 28, 0);
            GlStateManager.colorPointer(4, 5121, 28, 12);
            GlStateManager.texCoordPointer(2, 5126, 28, 16);
            OpenGlHelper.glClientActiveTexture(getLightmapTexUnit());
            GlStateManager.texCoordPointer(2, 5122, 28, 24);
            OpenGlHelper.glClientActiveTexture(getDefaultTexUnit());
        }

        public void remove() {
            for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                if (vertexBuffers[i] != null) {
                    vertexBuffers[i].deleteGlBuffers();
                }
            }
        }

        public void render() {
            GlStateManager.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableBlend();
            GlStateManager.enableCull();

            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(7425);
            } else {
                GlStateManager.shadeModel(7424);
            }
            for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                renderLayer(BlockRenderLayer.values()[i]);
            }
            GlStateManager.disableBlend();
            GlStateManager.disableCull();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        public int getDefaultTexUnit() {
            return OpenGlHelper.GL_TEXTURE0;
        }

        public int getLightmapTexUnit() {
            return OpenGlHelper.GL_TEXTURE1;
        }
    }

}
