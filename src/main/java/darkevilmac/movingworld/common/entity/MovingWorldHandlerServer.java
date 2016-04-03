package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import darkevilmac.movingworld.common.network.ChunkBlockUpdateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Collection;

public abstract class MovingWorldHandlerServer extends MovingWorldHandlerCommon {
    protected boolean firstChunkUpdate;

    public MovingWorldHandlerServer(EntityMovingWorld entitymovingWorld) {
        super(entitymovingWorld);
        firstChunkUpdate = true;
    }

    @Override
    public boolean interact(EntityPlayer player, ItemStack stack, EnumHand hand) {
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
            ChunkBlockUpdateMessage msg = new ChunkBlockUpdateMessage(getMovingWorld(), list);
            MovingWorld.instance.network.sendToAllAround(msg, new NetworkRegistry.TargetPoint(getMovingWorld().worldObj.provider.getDimension(), getMovingWorld().posX, getMovingWorld().posY, getMovingWorld().posZ, 64D));
        }
        list.clear();
        firstChunkUpdate = false;
    }
}
