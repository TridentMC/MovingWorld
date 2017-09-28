package com.elytradev.movingworld.common.entity;


import com.elytradev.movingworld.common.network.message.FarInteractMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public abstract class MovingWorldHandlerClient extends MovingWorldHandlerCommon {
    public MovingWorldHandlerClient(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.getDistanceSq(getMovingWorld()) >= 36D) {
            new FarInteractMessage(getMovingWorld(), hand).sendToServer();
        }

        return super.processInitialInteract(player, hand);
    }
}
