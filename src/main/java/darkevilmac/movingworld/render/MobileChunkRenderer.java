package darkevilmac.movingworld.render;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.MobileChunk;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MobileChunkRenderer {
    public boolean isInFrustum = false;
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

    private void tryEndDrawing() {
        try {
            Tessellator.getInstance().draw();
            MovingWorld.logger.trace("Drawing stopped");
        } catch (IllegalStateException ise) {
            MovingWorld.logger.trace("Not drawing");
        }
    }

    public void render(float partialticks) {
        if (isRemoved) {
            if (glRenderList != 0) {
                GLAllocation.deleteDisplayLists(glRenderList);
                glRenderList = 0;
            }
            return;
        }

        if (needsUpdate) {
            try {
                updateRender();
            } catch (Exception e) {
                MovingWorld.logger.error("A mobile chunk render error has occured", e);
                tryEndDrawing();
            }
        }

        if (glRenderList != 0) {
            for (int pass = 0; pass < 2; ++pass) {
                GL11.glCallList(glRenderList + pass);

                RenderHelper.enableStandardItemLighting();
                Iterator<TileEntity> it = tileEntityRenderers.iterator();
                while (it.hasNext()) {
                    TileEntity tile = it.next();
                    try {
                        if (tile.shouldRenderInPass(pass)) {
                            renderTileEntity(tile, partialticks);
                        }
                    } catch (Exception e) {
                        it.remove();
                        MovingWorld.logger.error("A tile entity render error has occured", e);
                        tryEndDrawing();
                    }
                }
            }
        }
    }

    /**
     * Render this TileEntity at its current position from the player
     */
    public void renderTileEntity(TileEntity tileEntity, float partialTicks) {
        int i = chunk.getCombinedLight(tileEntity.getPos(), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        TileEntityRendererDispatcher.instance.renderTileEntityAt(tileEntity, tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), partialTicks);
    }

    private void updateRender() {
        if (glRenderList == 0) {
            glRenderList = GLAllocation.generateDisplayLists(2);
        }

        for (int i = 0; i < 2; ++i) {
            skipRenderPass[i] = true;
        }

        Chunk.isLit = false;
        HashSet<TileEntity> hashset0 = new HashSet<TileEntity>();
        hashset0.addAll(tileEntityRenderers);
        tileEntityRenderers.clear();

        RenderBlocks renderblocks = new RenderBlocks(chunk);
        bytesDrawn = 0;

        for (int pass = 0; pass < 2; ++pass) {
            boolean flag = false;
            boolean flag1 = false;
            boolean glliststarted = false;

            for (int y = chunk.minY(); y < chunk.maxY(); ++y) {
                for (int z = chunk.minZ(); z < chunk.maxZ(); ++z) {
                    for (int x = chunk.minX(); x < chunk.maxX(); ++x) {
                        Block block = chunk.getBlock(x, y, z);
                        if (block != null && block.getMaterial() != Material.air) {
                            if (!glliststarted) {
                                glliststarted = true;
                                GL11.glNewList(glRenderList + pass, GL11.GL_COMPILE);
                                GL11.glPushMatrix();
                                float f = 1.000001F;
                                GL11.glTranslatef(-8.0F, -8.0F, -8.0F);
                                GL11.glScalef(f, f, f);
                                GL11.glTranslatef(8.0F, 8.0F, 8.0F);
                                Tessellator.instance.startDrawingQuads();
                            }

                            if (pass == 0 && block.hasTileEntity(chunk.getBlockMetadata(x, y, z))) {
                                TileEntity tileentity = chunk.getTileEntity(x, y, z);

                                if (TileEntityRendererDispatcher.instance.hasSpecialRenderer(tileentity)) {
                                    tileEntityRenderers.add(tileentity);
                                }
                            }

                            int blockpass = block.getRenderBlockPass();
                            if (blockpass > pass) {
                                flag = true;
                            }
                            if (!block.canRenderInPass(pass)) {
                                continue;
                            }
                            flag1 |= renderblocks.renderBlockByRenderType(block, x, y, z);
                        }
                    }
                }
            }

            if (glliststarted) {
                bytesDrawn += Tessellator.instance.draw();
                GL11.glPopMatrix();
                GL11.glEndList();
                Tessellator.instance.setTranslation(0D, 0D, 0D);
            } else {
                flag1 = false;
            }

            if (flag1) {
                skipRenderPass[pass] = false;
            }

            if (!flag) {
                break;
            }
        }

        HashSet<TileEntity> hashset1 = new HashSet<TileEntity>();
        hashset1.addAll(tileEntityRenderers);
        hashset1.removeAll(hashset0);
        tileEntities.addAll(hashset1);
        hashset0.removeAll(tileEntityRenderers);
        tileEntities.removeAll(hashset0);
        isInitialized = true;

        needsUpdate = false;
    }

    public void dispatchBlockRender(IBlockState state, BlockPos pos, TextureAtlasSprite atlasSprite, World world) {
        if (state.getBlock().isAir(world, pos))
            return; //Don't render air, trust me it's a bad idea.


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
