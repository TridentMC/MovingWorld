package darkevilmac.movingworld.entity;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.MobileChunkServer;
import darkevilmac.movingworld.network.ChunkBlockUpdateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

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
        Collection<BlockPos> list = ((MobileChunkServer) getMovingWorld().getMovingWorldChunk()).getSendQueue();
        if (!firstChunkUpdate) {
            ChunkBlockUpdateMessage msg = new ChunkBlockUpdateMessage(getMovingWorld(), list);
            MovingWorld.instance.network.sendToAllAround(msg, new NetworkRegistry.TargetPoint(getMovingWorld().worldObj.provider.getDimensionId(), getMovingWorld().posX, getMovingWorld().posY, getMovingWorld().posZ, 64D));
        }
        list.clear();
        firstChunkUpdate = false;
    }
}
