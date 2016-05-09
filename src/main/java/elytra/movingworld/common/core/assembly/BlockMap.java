package elytra.movingworld.common.core.assembly;


import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Get a blockstate and a tile (if applicable) from a position. Contains some nice utility methods.
 */
public class BlockMap implements Iterable<Pair<BlockPos, Pair<IBlockState, TileEntity>>> {

    protected HashMap<BlockPos, Pair<IBlockState, TileEntity>> internalMap;
    private Vec3i min;
    private Vec3i max;

    public BlockMap(Vec3i ORIGIN) {
        internalMap = new HashMap<BlockPos, Pair<IBlockState, TileEntity>>();
        min = new Vec3i(ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
        max = new Vec3i(ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
    }

    public Vec3i getMin() {
        return min;
    }

    public Vec3i getMax() {
        return max;
    }

    @Override
    public Iterator<Pair<BlockPos, Pair<IBlockState, TileEntity>>> iterator() {
        return new BlockMapIterator(this);
    }

    public void addToMap(BlockPos pos, IBlockState state, TileEntity tileEntity) {
        internalMap.put(pos, new MutablePair<IBlockState, TileEntity>(state, tileEntity));

        min = new Vec3i(pos.getX() < min.getX() ? pos.getX() : min.getX(),
                pos.getY() < min.getY() ? pos.getY() : min.getY(),
                pos.getZ() < min.getZ() ? pos.getZ() : min.getZ());

        max = new Vec3i(pos.getX() > max.getX() ? pos.getX() : max.getX(),
                pos.getY() > max.getY() ? pos.getY() : max.getY(),
                pos.getZ() > max.getZ() ? pos.getZ() : max.getZ());
    }

    public void addToMap(BlockPos pos, IBlockState state) {
        addToMap(pos, state, null);
    }

    public IBlockState getBlockState(BlockPos key) {
        return internalMap.get(key).getLeft();
    }

    public TileEntity getTile(BlockPos key) {
        return internalMap.get(key).getRight();
    }

    public int size() {
        return internalMap.size();
    }

    public boolean containsBlockAtPosition(BlockPos pos) {
        return internalMap.containsKey(pos);
    }

    /**
     * Shifts the position of all blocks/tiles in this map by @param pos specified.
     *
     * @param shiftMode if true add pos, if false subtract pos
     */
    public void shiftPosition(BlockPos pos, boolean shiftMode) {
        HashMap<BlockPos, Pair<IBlockState, TileEntity>> newMap = new HashMap<BlockPos, Pair<IBlockState, TileEntity>>(); // Temp storage for the moved tiles.
        Vec3i newMin = new Vec3i(0, 0, 0);
        Vec3i newMax = new Vec3i(0, 0, 0);

        Iterator<Pair<BlockPos, Pair<IBlockState, TileEntity>>> iterator = this.iterator();
        while (iterator.hasNext()) {
            Pair<BlockPos, Pair<IBlockState, TileEntity>> entry = iterator.next();
            BlockPos posOfIndex = entry.getLeft();
            TileEntity tileOfIndex = entry.getRight().getRight();

            BlockPos shiftedPos = new BlockPos(posOfIndex);
            shiftedPos = shiftMode ? shiftedPos.add(pos) : shiftedPos.subtract(pos);
            TileEntity shiftedTile = null;

            if (tileOfIndex != null) {
                // Adjust the tile's position.
                shiftedTile = TileEntity.createTileEntity(null /*null because it's never even used -.-*/, tileOfIndex.serializeNBT());
                shiftedTile.setPos(shiftedPos);
            }

            newMin = new Vec3i(shiftedPos.getX() < newMin.getX() ? shiftedPos.getX() : newMin.getX(),
                    shiftedPos.getY() < newMin.getY() ? shiftedPos.getY() : newMin.getY(),
                    shiftedPos.getZ() < newMin.getZ() ? shiftedPos.getZ() : newMin.getZ());

            newMax = new Vec3i(shiftedPos.getX() > newMax.getX() ? shiftedPos.getX() : newMax.getX(),
                    shiftedPos.getY() > newMax.getY() ? shiftedPos.getY() : newMax.getY(),
                    shiftedPos.getZ() > newMax.getZ() ? shiftedPos.getZ() : newMax.getZ());

            newMap.put(shiftedPos, new MutablePair<IBlockState, TileEntity>(entry.getRight().getLeft(), shiftedTile));
        }

        this.min = newMin;
        this.max = newMax;
        internalMap.clear();
        internalMap.putAll(newMap);
    }

    protected class BlockMapIterator implements Iterator<Pair<BlockPos, Pair<IBlockState, TileEntity>>> {
        int index;
        List<BlockPos> keySet = new ArrayList<BlockPos>();
        BlockMap parent;

        public BlockMapIterator(BlockMap parent) {
            index = 0;
            this.parent = parent;
        }

        @Override
        public boolean hasNext() {
            return index < parent.internalMap.size();
        }

        @Override
        public Pair<BlockPos, Pair<IBlockState, TileEntity>> next() {
            if (keySet.isEmpty())
                keySet.addAll(parent.internalMap.keySet());

            Pair<BlockPos, Pair<IBlockState, TileEntity>> pair =
                    new MutablePair<BlockPos, Pair<IBlockState, TileEntity>>(keySet.get(index), parent.internalMap.get(keySet.get(index)));
            index++;
            return pair;
        }

        @Override
        public void remove() {
            parent.internalMap.remove(keySet.get(index));
            keySet.remove(index);
            index--;
        }
    }
}
