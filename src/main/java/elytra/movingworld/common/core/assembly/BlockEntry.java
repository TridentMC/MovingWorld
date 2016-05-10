package elytra.movingworld.common.core.assembly;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * For use in BlockMap, cleaner than some Pairs in Pairs.
 */
public class BlockEntry {

    public BlockPos pos;
    public TileEntity tile;
    public IBlockState state;

    public BlockEntry(BlockPos pos, IBlockState state, TileEntity tile) {
        this.pos = pos;
        this.state = state;
        this.tile = tile;
    }

    public BlockEntry(BlockPos pos, IBlockState state) {
        this(pos, state, null);
    }

    public BlockEntry(BlockPos pos) {
        this(pos, Blocks.air.getDefaultState());
    }
}
