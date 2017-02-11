package com.elytradev.movingworld.common.experiments.newassembly;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to read from a world into a collection.
 */
public class WorldReader {

    public final BlockPos start;
    public final World world;

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
        IBlockState readState = world.getBlockState(pos);

        if (readState != null && readState.getBlock() != Blocks.AIR) {
            if (!collected.containsKey(pos)) {
                collected.put(pos, new Tuple<>(readState, world.getTileEntity(pos)));
            }

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
        BlockPos invertedStart = new BlockPos(start.getX() * -1, 0, start.getZ() * -1);

        World subWorld = DimensionManager.getWorld(MovingWorldExperimentsMod.registeredDimensions.get(world.provider.getDimension()));

        Map<BlockPos, Tuple<IBlockState, TileEntity>> shiftedCollected = Maps.newHashMap();
        collected.entrySet().stream().forEach(blockPosTupleEntry -> {
            BlockPos shiftedPos = blockPosTupleEntry.getKey().add(invertedStart);
            TileEntity shiftedTile = blockPosTupleEntry.getValue().getSecond();
            shiftedTile.setPos(shiftedPos);

            shiftedCollected.put(shiftedPos, new Tuple<>(blockPosTupleEntry.getValue().getFirst(), shiftedTile));
        });

        int minX = 0, minZ = 0;
        int maxX = 0, maxZ = 0;

        for (BlockPos pos : shiftedCollected.keySet()) {
            if(minX > pos.getX()){
                minX = pos.getX();
            }
            if(maxX < pos.getX()){
                maxX = pos.getX();
            }
            if(minZ > pos.getZ()){
                minZ = pos.getZ();
            }
            if(maxZ < pos.getZ()){
                maxZ = pos.getZ();
            }
        }
    }

}
