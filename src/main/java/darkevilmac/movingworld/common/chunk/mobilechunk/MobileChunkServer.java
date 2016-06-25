package darkevilmac.movingworld.common.chunk.mobilechunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import darkevilmac.movingworld.common.entity.EntityMovingWorld;

public class MobileChunkServer extends MobileChunk {
    private Set<BlockPos> sendQueue;

    public MobileChunkServer(World world, EntityMovingWorld entityMovingWorld) {
        super(world, entityMovingWorld);
        sendQueue = new HashSet<BlockPos>();
    }

    public Collection<BlockPos> getSendQueue() {
        return sendQueue;
    }

    @Override
    public boolean addBlockWithState(BlockPos pos, IBlockState blockState) {
        if (super.addBlockWithState(pos, blockState)) {
            sendQueue.add(pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        if (super.setBlockState(pos, state)) {
            sendQueue.add(pos);
            return true;
        }
        return false;
    }

    @Override
    protected void onSetBlockAsFilledAir(BlockPos pos) {
    }
}
