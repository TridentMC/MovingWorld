package com.elytradev.movingworld.common.network.message;

import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.LogicalSide;

@RegisteredMessage(channel = "movingworld:network", destination = LogicalSide.SERVER)
public class FarInteractMessage extends Message {

    public EntityMovingWorld movingWorld;
    public EnumHand hand;

    public FarInteractMessage(EntityMovingWorld movingWorld, EnumHand hand) {
        this.movingWorld = movingWorld;
        this.hand = hand;
    }

    @Override
    public void handle(EntityPlayer entityPlayer) {
        entityPlayer.interactOn(movingWorld, hand);
    }
}
