package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

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

        if (worldClient == null)
            return true;

        // Okay, back to actual comments. Adjust GL position to compensate for the odd coords our chunk contains.
        GlStateManager.pushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();

        // Blocks.
        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tess.getBuffer();
        vertexBuffer.setTranslation(0, 0, 0);
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (int cX = worldClient.region.regionMin.chunkXPos; cX <= worldClient.region.regionMax.chunkXPos; cX++) {
            for (int cZ = worldClient.region.regionMin.chunkZPos; cZ <= worldClient.region.regionMax.chunkZPos; cZ++) {
                ChunkPos chunkPos = new ChunkPos(cX, cZ);

                for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(chunkPos.getXStart(), 0, chunkPos.getZStart()),
                        new BlockPos(chunkPos.getXEnd(), worldClient.getActualHeight(), chunkPos.getZEnd()))) {
                    IBlockState state = worldClient.getBlockState(pos);

                    if (state == null || Objects.equals(state.getBlock(), Blocks.AIR))
                        continue;

                    BlockPos shiftPos = new BlockPos(pos).subtract(region.minBlockPos());
                    vertexBuffer.setTranslation(shiftPos.getX(), shiftPos.getY(), shiftPos.getZ());
                    dispatcher.renderBlock(state, pos, worldClient, vertexBuffer);
                }
            }
        }

        tess.draw();

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
