package com.elytradev.movingworld.common.network.message;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.network.MovingWorldNetworking;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class FarInteractMessage extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public EntityMovingWorld movingWorld;
    public EnumHand hand;

    public FarInteractMessage(NetworkContext ctx) {
        super(ctx);
    }

    public FarInteractMessage(EntityMovingWorld movingWorld, EnumHand hand) {
        super(MovingWorldNetworking.NETWORK);

        this.movingWorld = movingWorld;
        this.hand = hand;
    }

    @Override
    protected void handle(EntityPlayer entityPlayer) {
        entityPlayer.interactOn(movingWorld, hand);
    }
}
