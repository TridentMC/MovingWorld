package darkevilmac.movingworld.entity;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.network.FarInteractMessage;
import net.minecraft.entity.player.EntityPlayer;

public abstract class MovingWorldHandlerClient extends MovingWorldHandlerCommon {
    public MovingWorldHandlerClient(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (player.getDistanceSqToEntity(getMovingWorld()) >= 36D) {
            FarInteractMessage msg = new FarInteractMessage(getMovingWorld());
            MovingWorld.instance.network.sendToServer(msg);
        }

        return super.interact(player);
    }
}
