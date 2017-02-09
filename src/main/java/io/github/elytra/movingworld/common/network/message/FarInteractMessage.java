package io.github.elytra.movingworld.common.network.message;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;
import io.github.elytra.movingworld.common.network.MovingWorldNetworking;
import io.github.elytra.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.entity.Entity;
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
