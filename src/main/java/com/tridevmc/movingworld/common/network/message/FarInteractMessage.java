package com.tridevmc.movingworld.common.network.message;

import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;

@RegisteredMessage(channel = "movingworld:network", destination = LogicalSide.SERVER)
public class FarInteractMessage extends Message {

    public EntityMovingWorld movingWorld;
    public Hand hand;

    public FarInteractMessage(EntityMovingWorld movingWorld, Hand hand) {
        this.movingWorld = movingWorld;
        this.hand = hand;
    }

    @Override
    public void handle(PlayerEntity entityPlayer) {
        entityPlayer.interactOn(movingWorld, hand);
    }
}
