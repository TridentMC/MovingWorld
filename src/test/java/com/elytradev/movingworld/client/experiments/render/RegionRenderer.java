package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.MobileRegion;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

/**
 * just bad
 */
@SideOnly(Side.CLIENT)
public class RegionRenderer {

    MobileRegion region;
    MobileRegionWorldClient worldClient;

    public RegionRenderer(EntityMobileRegion region) {
        this.region = region.region;
        this.worldClient = (MobileRegionWorldClient) region.mobileRegionWorld;
    }

    /**
     * what is love?
     * <p>
     * baby don't hurt me,
     * don't hurt me.
     * <p>
     * no more.
     *
     * @returns if you hurt me
     */
    public boolean renderAll(float partialTicks) {
        // im sorry lord for i have sinned
        // there is no efficiency in this code and for that i am sorry
        // but im tired of trying to make good render code
        // i don't know how to do it
        // .
        // if you're reading this
        // please send help

        // Okay, back to actual comments. Adjust GL position to compensate for the odd coords our chunk contains.
        GlStateManager.pushMatrix();
        GlStateManager.translate(region.minBlockPos().getX() * -1, 0, region.minBlockPos().getZ() * -1);
        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();

        // Blocks.
        Arrays.stream(BlockRenderLayer.values()).forEach(layer -> {
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexBuffer = tessellator.getBuffer();
            vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

            ForgeHooksClient.setRenderLayer(layer);
            for (BlockPos pos : BlockPos.getAllInBox(region.minBlockPos(), region.maxBlockPos())) {
                IBlockState state = worldClient.getBlockState(pos);
                if (state == null)
                    continue;

                if (state.getBlock().canRenderInLayer(state, layer)) {
                    dispatcher.renderBlock(state, pos, worldClient, vertexBuffer);
                }
            }
            ForgeHooksClient.setRenderLayer(null);

            tessellator.draw();
        });


        //Tiles.
        TileEntityRendererDispatcher tileRenderDispatcher = TileEntityRendererDispatcher.instance;

        for (BlockPos chunkPos : BlockPos.getAllInBox(new BlockPos(region.regionMin.chunkXPos, 0, region.regionMin.chunkZPos),
                new BlockPos(region.regionMax.chunkXPos, 0, region.regionMax.chunkZPos))) {
            Chunk chunk = worldClient.getChunkFromChunkCoords(chunkPos.getX(), chunkPos.getZ());

            chunk.getTileEntityMap().entrySet().stream().filter(blockPosTileEntityEntry -> tileRenderDispatcher.getSpecialRenderer(blockPosTileEntityEntry.getValue()) != null).forEach(blockPosTileEntityEntry -> {
                BlockPos pos = blockPosTileEntityEntry.getKey();
                TileEntity tile = blockPosTileEntityEntry.getValue();

                tileRenderDispatcher.renderTileEntityAt(tile, pos.getX(), pos.getY(), pos.getZ(), partialTicks);
            });
        }

        GlStateManager.popMatrix();

        // it always hurts
        return true;
    }

}
