package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.movingworld.common.experiments.network.ChunkData;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class MessageBlockData extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public Entity regionEntity;
    public ChunkData data;

    public MessageBlockData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageBlockData(Entity regionEntity) {
        super(MovingWorldExperimentsNetworking.networkContext);

        this.regionEntity = regionEntity;
    }

    @Override
    protected void handle(EntityPlayer sender) {

    }
}
