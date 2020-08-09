package com.tridevmc.movingworld.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.ModList;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MobileChunkRenderer {
    public static boolean hasOptifine = ModList.get().isLoaded("optifine");
    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    public boolean needsUpdate;
    public boolean isRemoved;
    public LegacyRender legacyRender = new LegacyRender();
    public VBORender vboRender = new VBORender();
    private boolean usingVBOs = useVBO();
    private MobileChunkClient chunk;


    public MobileChunkRenderer(MobileChunk mobilechunk) {
        chunk = (MobileChunkClient) mobilechunk;
        needsUpdate = true;
    }

    public boolean useVBO() {
        return GLX.useVbo() && !hasOptifine;
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
            if (renderer != null) {
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
            if (renderer != null) {
                dispatcher.render(tile, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks);
            }
            tile.setWorld(chunk.world);
        }
        dispatcher.drawBatch();
        GlStateManager.popMatrix();
    }

    public void dispatchBlockRender(BlockPos blockPos, BlockState blockState, BufferBuilder buffer) {
        buffer.color(1.0F, 1.0F, 1.0F, 1.0F);
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        blockRendererDispatcher.renderBlock(blockState, blockPos, chunk.getFakeWorld(), buffer, this.chunk.world.rand, EmptyModelData.INSTANCE);
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

            HashMap<BlockRenderLayer, List<Tuple<BlockPos, BlockState>>> blockRenderMap = Maps.newHashMap();
            for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                blockRenderMap.put(blockRenderLayer, new ArrayList<>());
            }
            // Collect block states.
            for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
                for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
                    for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        Block block = state.getBlock();

                        for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                            if (!block.canRenderInLayer(state, blockRenderLayer)
                                    || state.getRenderType().equals(BlockRenderType.INVISIBLE)) continue;

                            blockRenderMap.get(blockRenderLayer).add(new Tuple<>(pos, state));
                        }
                    }
                }
            }

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            // Actually render.
            for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                for (Tuple<BlockPos, BlockState> blockRenderData : blockRenderMap.get(blockRenderLayer)) {
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
        private final VertexBuffer[] vertexBuffers = new VertexBuffer[RenderType.getBlockRenderTypes().size()];

        public void compile() {
            remove();

            HashMap<RenderType, List<Tuple<BlockPos, BlockState>>> blockRenderMap = Maps.newHashMap();
            for (RenderType blockRenderLayer : RenderType.getBlockRenderTypes()) {
                blockRenderMap.put(blockRenderLayer, new ArrayList<>());
            }
            // Collect block states.
            for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
                for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
                    for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);

                        for (RenderType blockRenderLayer :  RenderType.getBlockRenderTypes()) {
                            if (! RenderTypeLookup.canRenderInLayer(state, blockRenderLayer)
                                    || state.getRenderType().equals(BlockRenderType.INVISIBLE)) continue;

                            blockRenderMap.get(blockRenderLayer).add(new Tuple<>(pos, state));
                        }
                    }
                }
            }

            for (int i = 0; i < RenderType.getBlockRenderTypes().size(); ++i) {
                RenderType renderLayer = RenderType.getBlockRenderTypes().get(i);
                this.vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);

                List<Tuple<BlockPos, BlockState>> data = blockRenderMap.get(renderLayer);
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

                BlockRendererDispatcher blockDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
                for (Tuple<BlockPos, BlockState> datum : data) {
                    bufferBuilder.color(1.0F, 1.0F, 1.0F, 1.0F);
                    blockDispatcher.renderBlock(datum.getB(), datum.getA(), chunk.getFakeWorld(), bufferBuilder, chunk.getFakeWorld().getRandom(), EmptyModelData.INSTANCE);
                }
                bufferBuilder.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                        (float) TileEntityRendererDispatcher.staticPlayerY,
                        (float) TileEntityRendererDispatcher.staticPlayerZ);
                bufferBuilder.finishDrawing();
                vertexBuffers[i].bufferData(bufferBuilder.getByteBuffer());
                bufferBuilder.reset();
            }
        }

        private void renderLayer(RenderType layer) {
            VertexBuffer vbo = vertexBuffers[layer.ordinal()];
            if (vbo == null)
                return;

            GlStateManager.enableClientState(32884);
            GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
            GlStateManager.enableClientState(32888);
            GLX.glClientActiveTexture(GLX.GL_TEXTURE1);
            GlStateManager.enableClientState(32888);
            GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
            GlStateManager.enableClientState(32886);

            GlStateManager.pushMatrix();
            vbo.bindBuffer();
            setupArrayPointers();
            vbo.drawArrays(7);
            GlStateManager.popMatrix();
            VertexBuffer.unbindBuffer();
            GlStateManager.clearCurrentColor();

            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                VertexFormatElement.Usage vertexformatelement$usage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();
                switch (vertexformatelement$usage) {
                    case POSITION:
                        GlStateManager.disableClientState(32884);
                        break;
                    case UV:
                        GLX.glClientActiveTexture(GLX.GL_TEXTURE0 + i);
                        GlStateManager.disableClientState(32888);
                        GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
                        break;
                    case COLOR:
                        GlStateManager.disableClientState(32886);
                        GlStateManager.clearCurrentColor();
                }
            }
        }

        private void setupArrayPointers() {
            GlStateManager.vertexPointer(3, 5126, 28, 0);
            GlStateManager.colorPointer(4, 5121, 28, 12);
            GlStateManager.texCoordPointer(2, 5126, 28, 16);
            GLX.glClientActiveTexture(GLX.GL_TEXTURE1);
            GlStateManager.texCoordPointer(2, 5122, 28, 24);
            GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
        }

        public void remove() {
            for (int i = 0; i < RenderType.getBlockRenderTypes().size(); ++i) {
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
            for (int i = 0; i < RenderType.getBlockRenderTypes().size(); ++i) {
                renderLayer(RenderType.getBlockRenderTypes().get(i));
            }
            GlStateManager.disableBlend();
            GlStateManager.disableCull();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

}
