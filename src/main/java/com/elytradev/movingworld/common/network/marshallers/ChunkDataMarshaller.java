package com.elytradev.movingworld.common.network.marshallers;

import com.elytradev.movingworld.common.chunk.CompressedChunkData;
import com.tridevmc.compound.network.marshallers.Marshaller;
import com.tridevmc.compound.network.marshallers.RegisteredMarshaller;
import io.netty.buffer.ByteBuf;

@RegisteredMarshaller(channel = "movingworld", ids = {"compressedchunkdata"}, acceptedTypes = {CompressedChunkData.class})
public class ChunkDataMarshaller extends Marshaller<CompressedChunkData> {
    @Override
    public CompressedChunkData readFrom(ByteBuf byteBuf) {
        return new CompressedChunkData(byteBuf);
    }

    @Override
    public void writeTo(ByteBuf byteBuf, CompressedChunkData compressedChunkData) {
        compressedChunkData.writeTo(byteBuf);
    }
}
