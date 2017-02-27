package com.elytradev.movingworld.common.experiments.newassembly;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to read from a world into a collection.
 */
public class WorldReader {

    public final BlockPos start;
    public final World world;
    public final Out out = new Out();

    private List<BlockPos> stack = Lists.newArrayList();
    private HashMap<BlockPos, Tuple<IBlockState, TileEntity>> collected = Maps.newHashMap();

    public WorldReader(BlockPos start, World world) {
        this.start = start;
        this.world = world;
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
            collected.put(pos, new Tuple<>(readState, world.getTileEntity(pos)));

            stack.add(pos.add(1, 0, 0));
            stack.add(pos.add(0, 1, 0));
            stack.add(pos.add(0, 0, 1));
            stack.add(pos.add(-1, 0, 0));
            stack.add(pos.add(0, -1, 0));
            stack.add(pos.add(0, 0, -1));
        }
    }

    public void moveToSubWorld() {
        // The following code shifts the position of the blocks found with our flood fill,
        // we need it shifted so the collection will be placed in the center of our MobileRegion.
        BlockPos invertedStart = new BlockPos(start.getX(), 0, start.getZ());
        World subWorld = MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(MovingWorldExperimentsMod.registeredDimensions.get(world.provider.getDimension()));
        RegionPool regionPool = RegionPool.getPool(subWorld.provider.getDimension(), true);
        MobileRegion region = regionPool.nextRegion(false);

        Map<BlockPos, Tuple<IBlockState, TileEntity>> shiftedCollected = Maps.newHashMap();
        collected.entrySet().stream().forEach(blockPosTupleEntry -> {
            BlockPos shiftedPos = blockPosTupleEntry.getKey().subtract(invertedStart);
            TileEntity shiftedTile = blockPosTupleEntry.getValue().getSecond();
            if (shiftedTile != null)
                shiftedTile.setPos(shiftedPos);

            shiftedCollected.put(shiftedPos, new Tuple<>(blockPosTupleEntry.getValue().getFirst(), shiftedTile));
        });

        int minX = 0, minZ = 0;
        int maxX = 0, maxZ = 0;

        for (BlockPos pos : shiftedCollected.keySet()) {
            if (minX > pos.getX()) {
                minX = pos.getX();
            }
            if (maxX < pos.getX()) {
                maxX = pos.getX();
            }
            if (minZ > pos.getZ()) {
                minZ = pos.getZ();
            }
            if (maxZ < pos.getZ()) {
                maxZ = pos.getZ();
            }
        }

        int avgX = Math.round((minX + maxX) / 2);
        int avgZ = Math.round((minZ + maxZ) / 2);
        BlockPos avg = new BlockPos(avgX, 0, avgZ);

        // Shift again to be centered with our center. (that was awful to say)
        Map<BlockPos, Tuple<IBlockState, TileEntity>> reshifted = Maps.newHashMap();
        shiftedCollected.entrySet().forEach(blockPosTupleEntry -> {
            BlockPos shiftedPos = blockPosTupleEntry.getKey().add(avg);
            shiftedPos = shiftedPos.add(region.centeredBlockPos());
            TileEntity shiftedTile = blockPosTupleEntry.getValue().getSecond();
            if (shiftedTile != null)
                shiftedTile.setPos(shiftedPos);

            reshifted.put(shiftedPos, new Tuple<>(blockPosTupleEntry.getValue().getFirst(), shiftedTile));
        });

        // Now set them to the actual child
        reshifted.forEach((blockPos, iBlockStateTileEntityTuple) -> {
            subWorld.setBlockState(blockPos, iBlockStateTileEntityTuple.getFirst());
            if (iBlockStateTileEntityTuple.getSecond() != null) {
                subWorld.setTileEntity(blockPos, iBlockStateTileEntityTuple.getSecond());
            }
        });

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
