package darkevilmac.movingworld.chunk.mobilechunk.world;

import darkevilmac.movingworld.chunk.mobilechunk.MobileChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * A wrapper for MobileChunks, used to give blocks accurate information about it's neighbors.
 */
public class FakeWorld extends World {

    MobileChunk mobileChunk;

    private FakeWorld(boolean remote, World parentWorld) {
        super(parentWorld.getSaveHandler(), parentWorld.getWorldInfo(), parentWorld.provider, parentWorld.theProfiler, remote);
    }

    public static FakeWorld getFakeWorld(MobileChunk chunk) {
        FakeWorld retVal = new FakeWorld(chunk.worldObj.isRemote, chunk.worldObj);
        retVal.mobileChunk = chunk;
        return retVal;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return mobileChunk.getTileEntity(pos);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected int getRenderDistanceChunks() {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return mobileChunk.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return false;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return false;
    }

}
