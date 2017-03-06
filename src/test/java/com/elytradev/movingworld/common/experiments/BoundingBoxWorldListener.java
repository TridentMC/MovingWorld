package com.elytradev.movingworld.common.experiments;

import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by darkevilmac on 3/5/2017.
 */
public class BoundingBoxWorldListener implements IWorldEventListener {

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        MobileRegion regionForPos = RegionPool.getPool(worldIn.provider.getDimension(), false).regions.get(worldIn.getChunkFromBlockCoords(pos).getPos());
        if (regionForPos == null)
            return;

        boolean removed = newState.getBlock().isAir(newState, worldIn, pos);

        if (regionForPos.size().isVecInside(new Vec3d(pos))) {
            if (removed && isPosOnEdge(pos, regionForPos)) {
                //In region, block was removed.


            }

            //We can ignore other cases, they don't matter for bounds calculations.
        } else {
            if (!removed) {
                //Outside of current region bounds, added block.

                regionForPos.sizeMin = BlockPosHelper.min(regionForPos.sizeMin, pos);
                regionForPos.sizeMax = BlockPosHelper.max(regionForPos.sizeMax, pos);
            }

            // Ignore blocks removed out of bounds because that isn't possible.
        }
    }

    /**
     * Detects if a pos is on the edge of region's current size.
     * <p>
     * Also returns whether the pos is on edge.
     *
     * @param pos
     * @param region
     * @return true if the pos is on edge.
     */
    public boolean isPosOnEdge(BlockPos pos, MobileRegion region) {
        // vry teknical code rite here.
        return pos.getX() == region.sizeMin.getX() || pos.getY() == region.sizeMin.getY() || pos.getZ() == region.sizeMin.getZ()
                || pos.getX() == region.sizeMax.getX() || pos.getY() == region.sizeMax.getY() || pos.getZ() == region.sizeMax.getZ();
    }

    @Override
    public void notifyLightSet(BlockPos pos) {

    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {

    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_, double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_, int... p_190570_16_) {

    }

    @Override
    public void onEntityAdded(Entity entityIn) {

    }

    @Override
    public void onEntityRemoved(Entity entityIn) {

    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }
}
