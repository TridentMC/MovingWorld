package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.common.chunk.ChunkIO;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import darkevilmac.movingworld.common.network.MovingWorldNetworking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

import java.util.Collection;

public abstract class MovingWorldHandlerServer extends MovingWorldHandlerCommon {
    protected boolean firstChunkUpdate;

    public MovingWorldHandlerServer(EntityMovingWorld entitymovingWorld) {
        super(entitymovingWorld);
        firstChunkUpdate = true;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (getMovingWorld().riddenByEntity == null) {
            player.mountEntity(getMovingWorld());
            return true;
        } else if (player.ridingEntity == null) {
            return getMovingWorld().getCapabilities().mountEntity(player);
        }

        return false;
    }

    @Override
    public void onChunkUpdate() {
        super.onChunkUpdate();
        Collection<BlockPos> list = ((MobileChunkServer) getMovingWorld().getMobileChunk()).getSendQueue();
        if (!firstChunkUpdate) {
            //ChunkBlockUpdateMessage msg = new ChunkBlockUpdateMessage(getMovingWorld(), list);
            //MovingWorld.instance.network.sendToAllAround(msg, new NetworkRegistry.TargetPoint(getMovingWorld().worldObj.provider.getDimensionId(), getMovingWorld().posX, getMovingWorld().posY, getMovingWorld().posZ, 64D));

            MovingWorldNetworking.NETWORK.send().packet("ChunkBlockUpdateMessage")
                    .with("dimID", getMovingWorld().worldObj.provider.getDimensionId())
                    .with("entityID", getMovingWorld().getEntityId())
                    .with("chunk", ChunkIO.writeCompressed(getMovingWorld().getMobileChunk(), list))
                    .toAllAround(getMovingWorld().worldObj, new Vec3i(getMovingWorld().posX, getMovingWorld().posY, getMovingWorld().posZ), 64D);
        }
        list.clear();
        firstChunkUpdate = false;
    }
}
