package com.elytradev.movingworld.client.experiments.render;

import com.elytradev.movingworld.client.experiments.MobileRegionWorldClient;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
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
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

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

                Chunk theChunk = worldClient.parentWorld.getChunkFromChunkCoords(cX, cZ);
                if (theChunk == null)
                    continue;

                for (ExtendedBlockStorage e : theChunk.getBlockStorageArray()) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 0; y < 16; y++) {
                                if (e == null)
                                    continue;

                                if (e.get(x, y, z) == null || e.get(x, y, z).getBlock() == Blocks.AIR)
                                    continue;
                                System.out.println("Block E Data for Render... " + e.get(x, y, z));
                            }
                        }
                    }
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
