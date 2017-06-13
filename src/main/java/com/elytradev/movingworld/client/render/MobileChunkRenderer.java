package com.elytradev.movingworld.client.render;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class MobileChunkRenderer {
	/**
	 * Boolean for whether this renderer needs to be updated or not
	 */
	public boolean needsUpdate;
	public boolean isRemoved;

	private MobileChunk chunk;

	public MobileChunkRenderer(MobileChunk mobilechunk) {
		chunk = mobilechunk;
		needsUpdate = true;
	}

	public void render(float partialTicks) {
		if (needsUpdate) {
			try {
				updateSimpleRender(partialTicks);
			} catch (Exception e) {
				MovingWorldMod.LOG.error("Exception when rendering a MobileChunk! ", e);
			}
		}
	}

	private void updateSimpleRender(float partialTicks) {
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

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
			for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
				for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
					BlockPos pos = new BlockPos(x, y, z);
					IBlockState blockState = chunk.getBlockState(pos);
					Block block = blockState.getBlock();

					for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
						if (!block.canRenderInLayer(blockState, blockRenderLayer)) continue;
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

						if (!block.getRenderType(blockState).equals(EnumBlockRenderType.INVISIBLE)) {
							dispatchBlockRender(blockState, pos, buffer);
						}
					}
					net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
				}
			}
		}
		buffer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.disableCull();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.pushMatrix();
		World tesrDispatchWorld = TileEntityRendererDispatcher.instance.world;
		TileEntityRendererDispatcher.instance.setWorld(chunk.getFakeWorld());
		for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
			for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
				for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
					BlockPos pos = new BlockPos(x, y, z);
					TileEntity tile = chunk.getTileEntity(pos);
					if (tile != null) {
						tile.setWorld(chunk.getFakeWorld());
						TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);

						if (renderer != null && tile.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
							TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks);
						}
						tile.setWorld(chunk.world);
					}
				}
			}
		}
		TileEntityRendererDispatcher.instance.setWorld(tesrDispatchWorld);
		GlStateManager.popMatrix();
	}

	public void dispatchBlockRender(IBlockState blockState, BlockPos blockPos, BufferBuilder buffer) {
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
