package io.github.elytra.movingworld.common.entity;

import com.unascribed.lambdanetwork.PendingPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import io.github.elytra.movingworld.common.network.MovingWorldNetworking;

public abstract class MovingWorldHandlerClient extends MovingWorldHandlerCommon {
    public MovingWorldHandlerClient(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.getDistanceSqToEntity(getMovingWorld()) >= 36D) {
            PendingPacket packet = MovingWorldNetworking.NETWORK.send().packet("FarInteractMessage")
                    .with("dimID", getMovingWorld().world.provider.getDimension())
                    .with("entityID", getMovingWorld().getEntityId())
                    .with("hand", hand.ordinal());
            packet.toServer();
        }

        return super.processInitialInteract(player, hand);
    }
}
