package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.network.FarInteractMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public abstract class MovingWorldHandlerClient extends MovingWorldHandlerCommon {
    public MovingWorldHandlerClient(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public boolean interact(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (player.getDistanceSqToEntity(getMovingWorld()) >= 36D) {
            FarInteractMessage msg = new FarInteractMessage(getMovingWorld(), stack, hand);
            MovingWorld.instance.network.sendToServer(msg);
        }

        return super.interact(player, stack, hand);
    }
}
