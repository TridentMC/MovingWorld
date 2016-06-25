package darkevilmac.movingworld.common.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

import darkevilmac.movingworld.common.chunk.ChunkIO;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import darkevilmac.movingworld.common.network.MovingWorldNetworking;

public abstract class MovingWorldHandlerServer extends MovingWorldHandlerCommon {
    protected boolean firstChunkUpdate;

    public MovingWorldHandlerServer(EntityMovingWorld entitymovingWorld) {
        super(entitymovingWorld);
        firstChunkUpdate = true;
    }

    @Override
    public boolean interact(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (getMovingWorld().getControllingPassenger() == null) {
            player.startRiding(getMovingWorld());
            return true;
        } else if (player.getControllingPassenger() == null) {
            return getMovingWorld().getCapabilities().mountEntity(player);
        }

        return false;
    }

    @Override
    public void onChunkUpdate() {
        super.onChunkUpdate();
        Collection<BlockPos> list = ((MobileChunkServer) getMovingWorld().getMobileChunk()).getSendQueue();
        if (!firstChunkUpdate) {
            MovingWorldNetworking.NETWORK.send().packet("ChunkBlockUpdateMessage")
                    .with("dimID", getMovingWorld().worldObj.provider.getDimension())
                    .with("entityID", getMovingWorld().getEntityId())
                    .with("chunk", ChunkIO.writeCompressed(getMovingWorld().getMobileChunk(), list))
                    .toAllAround(getMovingWorld().worldObj, getMovingWorld(), 64D);
        }
        list.clear();
        firstChunkUpdate = false;
    }
}

