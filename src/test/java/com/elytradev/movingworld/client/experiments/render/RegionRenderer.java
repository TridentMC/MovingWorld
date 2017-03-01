package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.BlockData;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * just bad
 */
@SideOnly(Side.CLIENT)
public class RegionRenderer {

    MobileRegion region;
    MobileRegionWorldClient worldClient;
    OffsetAccess offsetAccess;

    private Accessor<Boolean> chunkIsModified;

    private List<TileEntity> tiles;
    private Map<BlockRenderLayer, List<BlockData>> blocks;

    public RegionRenderer(EntityMobileRegion region) {
        this.region = region.region;
        this.worldClient = (MobileRegionWorldClient) region.mobileRegionWorld;
        this.offsetAccess = new OffsetAccess();

        chunkIsModified = Accessors.findField(Chunk.class, "isModified", "field_76643_l");
        if (region != null && worldClient != null)
            constructData();
    }

    private void constructData() {
        blocks = new HashMap<>();
        tiles = new ArrayList<>();

        for (BlockRenderLayer renderLayer : BlockRenderLayer.values()) {
            blocks.put(renderLayer, new ArrayList<>());
        }

        for (int cX = worldClient.region.regionMin.chunkXPos; cX <= worldClient.region.regionMax.chunkXPos; cX++) {
            for (int cZ = worldClient.region.regionMin.chunkZPos; cZ <= worldClient.region.regionMax.chunkZPos; cZ++) {
                ChunkPos chunkPos = new ChunkPos(cX, cZ);

                Chunk theChunk = worldClient.parentWorld.getChunkFromChunkCoords(cX, cZ);
                if (theChunk == null)
                    continue;

                for (ExtendedBlockStorage e : theChunk.getBlockStorageArray()) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 0; y < 16; y++) {
                                if (e == null || e.isEmpty())
                                    continue;

                                IBlockState stateAtPos = e.get(x, y, z);
                                if (stateAtPos == null || stateAtPos.getBlock() == Blocks.AIR)
                                    continue;

                                for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                                    if (stateAtPos.getBlock().canRenderInLayer(stateAtPos, layer)) {
                                        blocks.get(layer).add(new BlockData(new BlockPos(x, y, z).add(chunkPos.getXStart(), e.getYLocation(), chunkPos.getZStart()), stateAtPos));
                                    }
                                }
                            }
                        }
                    }
                }

                for (TileEntity t : theChunk.getTileEntityMap().values()) {
                    if (TileEntityRendererDispatcher.instance.getSpecialRenderer(t) != null) {
                        tiles.add(t);
                    }
                }
            }
        }
    }

    public boolean hasChanges() {
        for (int cX = worldClient.region.regionMin.chunkXPos; cX <= worldClient.region.regionMax.chunkXPos; cX++) {
            for (int cZ = worldClient.region.regionMin.chunkZPos; cZ <= worldClient.region.regionMax.chunkZPos; cZ++) {
                ChunkPos chunkPos = new ChunkPos(cX, cZ);
                Chunk theChunk = worldClient.getChunkFromChunkCoords(cX, cZ);

                if (chunkIsModified.get(theChunk)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean renderAll(float partialTicks) {
        if (worldClient == null)
            return true;

        if (hasChanges()) {
            constructData();
        }

        BlockRendererDispatcher rendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tessellator.getBuffer();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (BlockRenderLayer renderLayer : BlockRenderLayer.values()) {
            for (BlockData blockData : blocks.get(renderLayer)) {
                dispatchBlock(vertexBuffer, rendererDispatcher, blockData);
            }
        }

        vertexBuffer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        RenderHelper.enableStandardItemLighting();
        // it always hurts
        return true;
    }

    public void dispatchBlock(VertexBuffer vertexBuffer, BlockRendererDispatcher rendererDispatcher, BlockData data) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1, 1);
        rendererDispatcher.renderBlock(data.getState(), data.getPos().subtract(region.minBlockPos()), offsetAccess, vertexBuffer);
        GlStateManager.popMatrix();
    }

    public class OffsetAccess implements IBlockAccess {
        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return worldClient.parentWorld.getTileEntity(pos.add(region.minBlockPos()));
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return worldClient.parentWorld.getCombinedLight(pos.add(region.minBlockPos()), lightValue);
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return worldClient.parentWorld.getBlockState(pos.add(region.minBlockPos()));
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return worldClient.parentWorld.isAirBlock(pos.add(region.minBlockPos()));
        }

        @Override
        public Biome getBiome(BlockPos pos) {
            return worldClient.parentWorld.getBiome(pos.add(region.minBlockPos()));
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return worldClient.parentWorld.getStrongPower(pos.add(region.minBlockPos()), direction);
        }

        @Override
        public WorldType getWorldType() {
            return worldClient.parentWorld.getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return worldClient.parentWorld.isSideSolid(pos.add(region.minBlockPos()), side, _default);
        }
    }
}
