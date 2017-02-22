package com.elytradev.movingworld.common.experiments.network.messages.client;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageBlockData;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 2/21/2017.
 */
@ReceivedOn(Side.SERVER)
public class MessageRequestData extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public EntityMobileRegion regionEntity;

    public MessageRequestData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageRequestData(EntityMobileRegion regionEntity) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.regionEntity = regionEntity;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (regionEntity != null) {
            // return to sender, address unknown.
            new MessageBlockData(regionEntity).sendTo(sender);
        }
    }
}
