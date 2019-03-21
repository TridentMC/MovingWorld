package com.tridevmc.movingworld.common.chunk.mobilechunk;

import com.tridevmc.movingworld.client.render.MobileChunkRenderer;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MobileChunkClient extends MobileChunk {
    public Map<BlockPos, TileEntity> fastTESRS = Maps.newHashMap();
    public Map<BlockPos, TileEntity> normalTESRS = Maps.newHashMap();
    private MobileChunkRenderer renderer;

    public MobileChunkClient(World world, EntityMovingWorld movingWorld) {
        super(world, movingWorld);
        this.renderer = new MobileChunkRenderer(this);
    }

    public MobileChunkRenderer getRenderer() {
        return this.renderer;
    }

    @Override
    public void onChunkUnload() {
        List<TileEntity> iterator = new ArrayList<>(this.chunkTileEntityMap.values());
        for (TileEntity te : iterator) {
            this.removeChunkBlockTileEntity(te.getPos());
        }
        super.onChunkUnload();
        this.renderer.markRemoved();
    }

    @Override
    public void setChunkModified() {
        super.setChunkModified();
        this.renderer.markDirty();
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity tileentity) {
        super.setTileEntity(pos, tileentity);

        if (tileentity == null) {
            // Removed a tile, remove it from the render lists as well.
            this.fastTESRS.remove(pos);
            this.normalTESRS.remove(pos);
        } else {
            // Add a tesr, after confirming it has one, and that it's fast.
            if (TileEntityRendererDispatcher.instance.getRenderer(tileentity) != null) {
                if (tileentity.hasFastRenderer()) {
                    this.fastTESRS.put(pos, tileentity);
                } else {
                    this.normalTESRS.put(pos, tileentity);
                }
            }
        }
    }

    @Override
    public LogicalSide side() {
        return LogicalSide.CLIENT;
    }
}