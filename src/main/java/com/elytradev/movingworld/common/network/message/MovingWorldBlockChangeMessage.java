package com.elytradev.movingworld.common.network.message;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.ChunkIO;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.network.MovingWorldNetworking;
import com.elytradev.movingworld.common.network.marshallers.ByteBufMarshaller;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

/**
 * Sends MobileChunk block data to clients.
 */
@ReceivedOn(Side.CLIENT)
public class MovingWorldBlockChangeMessage extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public EntityMovingWorld movingWorld;
    @MarshalledAs(ByteBufMarshaller.MARSHALLER_NAME)
    public ByteBuf compressedChunkData;

    public MovingWorldBlockChangeMessage(NetworkContext ctx) {
        super(ctx);
    }

    public MovingWorldBlockChangeMessage(EntityMovingWorld movingWorld, ByteBuf compressedChunkData) {
        super(MovingWorldNetworking.NETWORK);
        this.movingWorld = movingWorld;
        this.compressedChunkData = compressedChunkData;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (movingWorld == null || movingWorld.getMobileChunk() == null || compressedChunkData == null)
            return;

        try {
            compressedChunkData.resetReaderIndex();
            compressedChunkData.resetWriterIndex();
            ChunkIO.readCompressed(compressedChunkData, movingWorld.getMobileChunk());
        } catch (IOException e) {
            MovingWorldMod.LOG.error(e);
        }
    }
}
