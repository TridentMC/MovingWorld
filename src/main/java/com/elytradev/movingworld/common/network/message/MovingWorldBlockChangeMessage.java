package com.elytradev.movingworld.common.network.message;


import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.ChunkIO;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.LogicalSide;

import java.io.IOException;

/**
 * Sends MobileChunk block data to clients.
 */
@RegisteredMessage(channel = "movingworld", destination = LogicalSide.CLIENT)
public class MovingWorldBlockChangeMessage extends Message {

    public EntityMovingWorld movingWorld;
    public ByteBuf compressedChunkData;

    public MovingWorldBlockChangeMessage() {
        super();
    }

    public MovingWorldBlockChangeMessage(EntityMovingWorld movingWorld, ByteBuf compressedChunkData) {
        super();
        this.movingWorld = movingWorld;
        this.compressedChunkData = compressedChunkData;
    }

    @Override
    public void handle(EntityPlayer sender) {
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
