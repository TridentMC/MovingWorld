package darkevilmac.movingworld.common.core.assembly;


import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
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

    public BlockMap() {
        internalMap = new HashMap<BlockPos, Pair<IBlockState, TileEntity>>();
    }

    @Override
    public Iterator<Pair<BlockPos, Pair<IBlockState, TileEntity>>> iterator() {
        return new BlockMapIterator(this);
    }

    public void addToMap(BlockPos pos, IBlockState state, TileEntity tileEntity) {
        internalMap.put(pos, new MutablePair<IBlockState, TileEntity>(state, tileEntity));
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
