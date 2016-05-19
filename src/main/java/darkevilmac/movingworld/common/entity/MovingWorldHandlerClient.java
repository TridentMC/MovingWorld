package darkevilmac.movingworld.common.entity;

import darkevilmac.movingworld.common.network.MovingWorldNetworking;
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
            MovingWorldNetworking.NETWORK.send().packet("FarInteractMessage")
                    .with("dimID", getMovingWorld().worldObj.provider.getDimension())
                    .with("entityID", getMovingWorld().getEntityId())
                    .with("hand", hand.ordinal())
                    .with("stack", stack.serializeNBT()).toServer();
        }

        return super.interact(player, stack, hand);
    }
}
