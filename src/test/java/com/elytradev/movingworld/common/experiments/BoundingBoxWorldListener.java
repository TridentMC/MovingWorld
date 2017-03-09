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
import net.minecraft.util.math.Vec3i;
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

        if (worldIn.isRemote)
            System.out.println("Before " + regionForPos.size());
        if (regionForPos.size().isVecInside(new Vec3d(pos))) {
            if (removed) {
                //In region, block was removed.

                // Spaghetti code begins
                boolean minXInvalidated = false, minXChange = pos.getX() == regionForPos.sizeMin.getX();
                boolean minYInvalidated = false, minYChange = pos.getY() == regionForPos.sizeMin.getY();
                boolean minZInvalidated = false, minZChange = pos.getZ() == regionForPos.sizeMin.getZ();
                boolean maxXInvalidated = false, maxXChange = pos.getX() == regionForPos.sizeMax.getX();
                boolean maxYInvalidated = false, maxYChange = pos.getY() == regionForPos.sizeMax.getY();
                boolean maxZInvalidated = false, maxZChange = pos.getZ() == regionForPos.sizeMax.getZ();

                if (minXChange) {
                    for (int y = regionForPos.sizeMin.getY(); y < regionForPos.sizeMax.getY(); y++) {
                        for (int z = regionForPos.sizeMin.getZ(); z < regionForPos.sizeMax.getZ(); z++) {
                            BlockPos curPos = new BlockPos(pos.getX(), y, z);
                            if (curPos.equals(pos))
                                continue;
                            IBlockState stateAtPos = worldIn.getBlockState(curPos);
                            if (!stateAtPos.getBlock().isAir(stateAtPos, worldIn, curPos)) {
                                minXInvalidated = true;
                                break;
                            }
                        }
                        if (minXInvalidated)
                            break;
                    }
                }

                if (minYChange) {
                    for (int x = regionForPos.sizeMin.getX(); x < regionForPos.sizeMax.getX(); x++) {
                        for (int z = regionForPos.sizeMin.getZ(); z < regionForPos.sizeMax.getZ(); z++) {
                            BlockPos curPos = new BlockPos(x, pos.getY(), z);
                            if (curPos.equals(pos))
                                continue;
                            IBlockState stateAtPos = worldIn.getBlockState(curPos);
                            if (!stateAtPos.getBlock().isAir(stateAtPos, worldIn, curPos)) {
                                minYInvalidated = true;
                                break;
                            }
                        }
                        if (minYInvalidated)
                            break;
                    }
                }

                if (minZChange) {
                    for (int x = regionForPos.sizeMin.getX(); x < regionForPos.sizeMax.getX(); x++) {
                        for (int y = regionForPos.sizeMin.getY(); y < regionForPos.sizeMax.getY(); y++) {
                            BlockPos curPos = new BlockPos(x, y, pos.getZ());
                            if (curPos.equals(pos))
                                continue;
                            IBlockState stateAtPos = worldIn.getBlockState(curPos);
                            if (!stateAtPos.getBlock().isAir(stateAtPos, worldIn, curPos)) {
                                minZInvalidated = true;
                                break;
                            }
                        }
                        if (minZInvalidated)
                            break;
                    }
                }

                if (maxXChange) {
                    for (int y = regionForPos.sizeMin.getY(); y < regionForPos.sizeMax.getY(); y++) {
                        for (int z = regionForPos.sizeMin.getZ(); z < regionForPos.sizeMax.getZ(); z++) {
                            BlockPos curPos = new BlockPos(pos.getX(), y, z);
                            if (curPos.equals(pos))
                                continue;
                            IBlockState stateAtPos = worldIn.getBlockState(curPos);
                            if (!stateAtPos.getBlock().isAir(stateAtPos, worldIn, curPos)) {
                                maxXInvalidated = true;
                                break;
                            }
                        }
                        if (maxXInvalidated)
                            break;
                    }
                }

                if (maxYChange) {
                    for (int x = regionForPos.sizeMin.getX(); x < regionForPos.sizeMax.getX(); x++) {
                        for (int z = regionForPos.sizeMin.getZ(); z < regionForPos.sizeMax.getZ(); z++) {
                            BlockPos curPos = new BlockPos(x, pos.getY(), z);
                            if (curPos.equals(pos))
                                continue;
                            IBlockState stateAtPos = worldIn.getBlockState(curPos);
                            if (!stateAtPos.getBlock().isAir(stateAtPos, worldIn, curPos)) {
                                maxYInvalidated = true;
                                break;
                            }
                        }
                        if (maxYInvalidated)
                            break;
                    }
                }

                if (maxZChange) {
                    for (int x = regionForPos.sizeMin.getX(); x < regionForPos.sizeMax.getX(); x++) {
                        for (int y = regionForPos.sizeMin.getY(); y < regionForPos.sizeMax.getY(); y++) {
                            BlockPos curPos = new BlockPos(x, y, pos.getZ());
                            if (curPos.equals(pos))
                                continue;

                            IBlockState stateAtPos = worldIn.getBlockState(curPos);
                            if (!stateAtPos.getBlock().isAir(stateAtPos, worldIn, curPos)) {
                                maxZInvalidated = true;
                                break;
                            }
                        }
                        if (maxZInvalidated)
                            break;
                    }
                }

                if (maxXInvalidated) {
                    regionForPos.sizeMax.subtract(new Vec3i(1, 0, 0));
                }
                if (maxYInvalidated) {
                    regionForPos.sizeMax.subtract(new Vec3i(0, 1, 0));
                }
                if (maxZInvalidated) {
                    regionForPos.sizeMax.subtract(new Vec3i(0, 0, 1));
                }

                if (minXInvalidated) {
                    regionForPos.sizeMin.add(new Vec3i(1, 0, 0));
                }
                if (minYInvalidated) {
                    regionForPos.sizeMin.add(new Vec3i(0, 1, 0));
                }
                if (minZInvalidated) {
                    regionForPos.sizeMin.add(new Vec3i(0, 0, 1));
                }
                // Spaghetti code ends.
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

        if (worldIn.isRemote)
            System.out.println("After " + regionForPos.size());
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
}
