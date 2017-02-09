package io.github.elytra.movingworld.common.network.message;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.chunk.ChunkIO;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;
import io.github.elytra.movingworld.common.network.MovingWorldNetworking;
import io.github.elytra.movingworld.common.network.marshallers.EntityMarshaller;
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
