package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.common.network.MovingWorldNetworking;
import net.minecraft.entity.player.EntityPlayer;

public abstract class MovingWorldHandlerClient extends MovingWorldHandlerCommon {
    public MovingWorldHandlerClient(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (player.getDistanceSqToEntity(getMovingWorld()) >= 36D) {
            MovingWorldNetworking.NETWORK.send().packet("FarInteractMessage")
                    .with("dimID", getMovingWorld().worldObj.provider.getDimensionId())
                    .with("entityID", getMovingWorld().getEntityId()).toServer();
        }

        return super.interact(player);
    }
}
