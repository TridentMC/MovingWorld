package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    public boolean setBlockIDWithState(BlockPos pos, IBlockState blockState) {
        if (super.setBlockIDWithState(pos, blockState)) {
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
