package com.elytradev.movingworld.common.experiments.newassembly;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.experiments.BlockPosHelper;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.BlockData;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to read from a world into a collection.
 */
public class WorldReader {

    public final BlockPos start;
    public final World world;
    public final Out out = new Out();

    public BlockPos.MutableBlockPos min, max;

    private List<BlockPos> stack = Lists.newArrayList();
    private HashMap<BlockPos, BlockData> collected = Maps.newHashMap();

    public WorldReader(BlockPos start, World world) {
        this.start = start;
        this.world = world;

        this.min = new BlockPos.MutableBlockPos(start);
        this.max = new BlockPos.MutableBlockPos(start);
    }

    public boolean readAll() {
        stack = Lists.newArrayList();
        stack.add(start);

        while (!stack.isEmpty()) {
            BlockPos active = stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
            readBlock(active);
        }

        return !collected.isEmpty();
    }

    private void readBlock(BlockPos pos) {
        if (collected.containsKey(pos))
            return;

        IBlockState readState = world.getBlockState(pos);

        if (readState != null && readState.getBlock() != Blocks.AIR) {
            collected.put(pos, new BlockData(pos, readState, world.getTileEntity(pos)));
            recalculateMinMax(pos);

            stack.add(pos.add(1, 0, 0));
            stack.add(pos.add(0, 1, 0));
            stack.add(pos.add(0, 0, 1));
            stack.add(pos.add(-1, 0, 0));
            stack.add(pos.add(0, -1, 0));
            stack.add(pos.add(0, 0, -1));
        }
    }

    public void recalculateMinMax(BlockPos compare) {
        min.setPos(BlockPosHelper.min(min, compare));
        max.setPos(BlockPosHelper.max(min, compare));
    }

    public BlockData shiftData(BlockData d, MobileRegion region) {
        BlockPos newPos = shiftPos(d.getPos(), region);

        if (d.hasTile()) {
            d.getTileEntity().setPos(newPos);
        }
        return new BlockData(newPos, d.getState(), d.getTileEntity());
    }

    public BlockPos shiftPos(BlockPos posIn, MobileRegion region) {
        BlockPos startPos = new BlockPos(start.getX(), 0, start.getZ());
        BlockPos shiftedMin = min.subtract(startPos);

        // Shift so the bottom corner of the blocks collected is 0,y,0
        BlockPos newPos = new BlockPos(posIn);
        newPos = newPos.subtract(startPos);
        newPos = newPos.subtract(new Vec3i(shiftedMin.getX(), 0, shiftedMin.getZ()));

        // Convert that to region location, centered.
        BlockPos collectedAreaSize = new BlockPos(max.subtract(min));
        collectedAreaSize = collectedAreaSize.subtract(new BlockPos(0, collectedAreaSize.getY(), 0));
        BlockPos regionCenter = region.centeredBlockPos();
        newPos = regionCenter.subtract(new BlockPos(collectedAreaSize.getX() / 2, 0, collectedAreaSize.getZ() / 2)).add(newPos);

        return newPos;
    }

    /**
     * Moves all data in the reader into the next available region in the appropriate world.
     */
    public void cloneToSubworld() {
        // The following code shifts the position of the blocks found with our flood fill,
        // we need it shifted so the collection will be placed in the center of our MobileRegion.
        BlockPos startPos = new BlockPos(start.getX(), 0, start.getZ());
        BlockPos shiftedMin = min.subtract(startPos);
        BlockPos shiftedMax = max.subtract(startPos);

        World subWorld = MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(MovingWorldExperimentsMod.registeredDimensions.get(world.provider.getDimension()));
        RegionPool regionPool = RegionPool.getPool(subWorld.provider.getDimension(), true);
        MobileRegion region = regionPool.nextRegion(false);

        List<BlockData> shiftedData = collected.values().stream().map(data -> shiftData(data, region)).collect(Collectors.toList());
        shiftedData.sort(new BlockDataComparator());

        BlockPos addedMin = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        BlockPos addedMax = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        List<BlockData> secondPass = new ArrayList<>();
        // Set blocks in region.
        for (BlockData d : shiftedData) {
            addedMin = BlockPosHelper.min(d.getPos(), addedMin);
            addedMax = BlockPosHelper.max(d.getPos(), addedMax);
            boolean success = subWorld.setBlockState(d.getPos(), d.getState(), 2);

            if (!success) {
                secondPass.add(d);
                continue;
            }

            if (d.hasTile()) {
                TileEntity tileEntity = d.getTileEntity();
                tileEntity.setPos(d.getPos());
                NBTTagCompound tileData = tileEntity.writeToNBT(new NBTTagCompound());

                subWorld.setTileEntity(d.getPos(), TileEntity.create(subWorld, tileData));
            }
        }

        secondPass.sort(new BlockDataComparator());

        // Second pass in-case of failures.
        for (BlockData d : secondPass) {
            boolean success = subWorld.setBlockState(d.getPos(), d.getState(), 2);

            if (!success) {
                MovingWorldExperimentsMod.logger.warn("Failed to add block to world on second pass... " + d.toString());
                continue;
            }

            if (d.hasTile()) {
                TileEntity tileEntity = d.getTileEntity();
                tileEntity.setPos(d.getPos());
                NBTTagCompound tileData = tileEntity.writeToNBT(new NBTTagCompound());

                subWorld.setTileEntity(d.getPos(), TileEntity.create(subWorld, tileData));
            }
        }

        out.setAddedRegionMin(addedMin);
        out.setAddedRegionMax(addedMax);
        out.setPool(regionPool);
        out.setRegion(region);
        out.setSubWorld(subWorld);
    }

    public void cleanRealWorld() {
        for (Map.Entry<BlockPos, BlockData> data : collected.entrySet()) {
            if (data.getValue().hasTile()) {
                world.removeTileEntity(data.getKey());
            }
            world.setBlockToAir(data.getKey());
        }
    }

    /**
     * Stores output data of the WorldReader after moving things to a subworld.
     */
    public class Out {
        private MobileRegion region;
        private RegionPool pool;
        private World subWorld;
        private BlockPos addedRegionMin;
        private BlockPos addedRegionMax;

        public BlockPos getAddedRegionMin() {
            return addedRegionMin;
        }

        private void setAddedRegionMin(BlockPos addedRegionMin) {
            this.addedRegionMin = addedRegionMin;
        }

        public BlockPos getAddedRegionMax() {
            return addedRegionMax;
        }

        private void setAddedRegionMax(BlockPos addedRegionMax) {
            this.addedRegionMax = addedRegionMax;
        }

        public MobileRegion getRegion() {
            return region;
        }

        private void setRegion(MobileRegion region) {
            this.region = region;
        }

        public RegionPool getPool() {
            return pool;
        }

        private void setPool(RegionPool pool) {
            this.pool = pool;
        }

        public World getSubWorld() {
            return subWorld;
        }

        private void setSubWorld(World subWorld) {
            this.subWorld = subWorld;
        }
    }

    public class BlockDataComparator implements Comparator<BlockData> {

        @Override
        public int compare(BlockData o1, BlockData o2) {
            Set<String> highPriority = MovingWorldMod.INSTANCE.getNetworkConfig().getShared().assemblePriorityConfig.getHighPriorityDisassembly();
            Set<String> lowPriority = MovingWorldMod.INSTANCE.getNetworkConfig().getShared().assemblePriorityConfig.getLowPriorityDisassembly();

            int o1Int = 0;
            int o2Int = 0;

            if (highPriority.contains(Block.REGISTRY.getNameForObject(o1.getState().getBlock()).toString())) {
                o1Int = 2;
            } else if (lowPriority.contains(Block.REGISTRY.getNameForObject(o1.getState().getBlock()).toString())) {
                o1Int = 1;
            }

            if (highPriority.contains(Block.REGISTRY.getNameForObject(o2.getState().getBlock()).toString())) {
                o2Int = 2;
            } else if (lowPriority.contains(Block.REGISTRY.getNameForObject(o2.getState().getBlock()).toString())) {
                o2Int = 1;
            }

            return o1Int - o2Int;
        }
    }

}
