package darkevilmac.movingworld.common.core.assembly;


import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Get a blockstate and a tile (if applicable) from a position. Contains some nice utility methods.
 */
public class BlockMap implements Iterable<Pair<IBlockState, TileEntity>> {

    private HashMap<BlockPos, Pair<IBlockState, TileEntity>> internalMap;

    public BlockMap() {
        internalMap = new HashMap<BlockPos, Pair<IBlockState, TileEntity>>();
    }

    @Override
    public Iterator<Pair<IBlockState, TileEntity>> iterator() {
        return new BlockMapIterator();
    }

    public IBlockState getBlockState(BlockPos key) {
        return internalMap.get(key).getLeft();
    }

    public TileEntity getTile(BlockPos key) {
        return internalMap.get(key).getRight();
    }

    protected class BlockMapIterator implements Iterator<Pair<IBlockState, TileEntity>> {
        int index;
        List<BlockPos> keySet = new ArrayList<BlockPos>();

        public BlockMapIterator() {
            index = 0;
            keySet.addAll(internalMap.keySet());
        }

        @Override
        public boolean hasNext() {
            return index <= internalMap.size();
        }

        @Override
        public Pair<IBlockState, TileEntity> next() {
            Pair<IBlockState, TileEntity> pair = internalMap.get(keySet.get(index));
            index++;
            return pair;
        }

        @Override
        public void remove() {
            internalMap.remove(keySet.get(index));
            keySet.remove(index);
            index--;
        }
    }
}
