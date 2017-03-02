package com.elytradev.movingworld.common.experiments.newassembly;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.BlockData;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (min.getX() > compare.getX()) {
            min.setPos(compare.getX(), min.getY(), min.getZ());
        }
        if (min.getY() > compare.getY()) {
            min.setPos(min.getX(), compare.getY(), min.getZ());
        }
        if (min.getZ() > compare.getZ()) {
            min.setPos(min.getX(), min.getY(), compare.getZ());
        }

        if (max.getX() < compare.getX()) {
            max.setPos(compare.getX(), max.getY(), max.getZ());
        }
        if (max.getY() < compare.getY()) {
            max.setPos(max.getX(), compare.getY(), max.getZ());
        }
        if (max.getZ() < compare.getZ()) {
            max.setPos(max.getX(), max.getY(), compare.getZ());
        }
    }

    public BlockData shiftData(BlockData d, MobileRegion region) {
        BlockPos startPos = new BlockPos(start.getX(), 0, start.getZ());
        BlockPos shiftedMin = min.subtract(startPos);

        // Shift so the bottom corner of the blocks collected is 0,y,0
        BlockPos newPos = new BlockPos(d.getPos());
        newPos = newPos.subtract(startPos);
        newPos = newPos.subtract(new Vec3i(shiftedMin.getX(), 0, shiftedMin.getZ()));

        // Convert that to region location, centered.
        BlockPos collectedAreaSize = new BlockPos(max.subtract(min));
        collectedAreaSize = collectedAreaSize.subtract(new BlockPos(0, collectedAreaSize.getY(), 0));
        BlockPos regionCenter = region.centeredBlockPos();
        newPos = regionCenter.subtract(new BlockPos(collectedAreaSize.getX() / 2, 0, collectedAreaSize.getZ() / 2)).add(newPos);

        if (d.hasTile()) {
            d.getTileEntity().setPos(newPos);
        }
        return new BlockData(newPos, d.getState(), d.getTileEntity());
    }

    /**
     * Moves all data in the reader into the next available region in the appropriate world.
     */
    public void moveToSubWorld() {
        // The following code shifts the position of the blocks found with our flood fill,
        // we need it shifted so the collection will be placed in the center of our MobileRegion.
        BlockPos startPos = new BlockPos(start.getX(), 0, start.getZ());
        BlockPos shiftedMin = min.subtract(startPos);
        BlockPos shiftedMax = max.subtract(startPos);

        World subWorld = MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(MovingWorldExperimentsMod.registeredDimensions.get(world.provider.getDimension()));
        RegionPool regionPool = RegionPool.getPool(subWorld.provider.getDimension(), true);
        MobileRegion region = regionPool.nextRegion(false);

        List<BlockData> shiftedData = collected.values().stream().map(data -> shiftData(data, region)).collect(Collectors.toList());

        List<BlockData> secondPass = new ArrayList<>();
        // Set blocks in region.
        for (BlockData d : shiftedData) {
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

        // Second pass in-case of failures.
        for (BlockData d : secondPass) {
            boolean success = subWorld.setBlockState(d.getPos(), d.getState(), 2);

            if (!success) {
                System.out.println("Failed to add block to world on second pass... " + d.toString());
                continue;
            }

            if (d.hasTile()) {
                TileEntity tileEntity = d.getTileEntity();
                tileEntity.setPos(d.getPos());
                NBTTagCompound tileData = tileEntity.writeToNBT(new NBTTagCompound());

                subWorld.setTileEntity(d.getPos(), TileEntity.create(subWorld, tileData));
            }
        }

        out.setPool(regionPool);
        out.setRegion(region);
        out.setSubWorld(subWorld);
    }

    /**
     * Stores output data of the WorldReader after moving things to a subworld.
     */
    public class Out {
        private MobileRegion region;
        private RegionPool pool;
        private World subWorld;

        public MobileRegion getRegion() {
            return region;
        }

        public void setRegion(MobileRegion region) {
            this.region = region;
        }

        public RegionPool getPool() {
            return pool;
        }

        public void setPool(RegionPool pool) {
            this.pool = pool;
        }

        public World getSubWorld() {
            return subWorld;
        }

        public void setSubWorld(World subWorld) {
            this.subWorld = subWorld;
        }
    }

}
