package darkevilmac.movingworld.common.core.assembly;


import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Get a blockstate and a tile (if applicable) from a position. Contains some nice utility methods.
 */
public class BlockMap implements Iterable<Pair<BlockPos, Pair<IBlockState, TileEntity>>> {

    private HashMap<BlockPos, Pair<IBlockState, TileEntity>> internalMap;

    public BlockMap() {
        internalMap = new HashMap<BlockPos, Pair<IBlockState, TileEntity>>();
    }

    @Override
    public Iterator<Pair<BlockPos, Pair<IBlockState, TileEntity>>> iterator() {
        return null;
    }

    public IBlockState getBlockState(BlockPos key) {
        return internalMap.get(key).getLeft();
    }

    public TileEntity getTile(BlockPos key) {
        return internalMap.get(key).getRight();
    }

}
