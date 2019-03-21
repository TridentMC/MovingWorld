package com.tridevmc.movingworld.common.network.marshallers;

import com.tridevmc.compound.network.marshallers.Marshaller;
import com.tridevmc.compound.network.marshallers.RegisteredMarshaller;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;


@RegisteredMarshaller(channel = "movingworld", ids = {"byebuf"}, acceptedTypes = {ByteBuf.class})
public class ByteBufMarshaller extends Marshaller<ByteBuf> {

    @Override
    public ByteBuf readFrom(ByteBuf in) {
        PacketBuffer pBuf = new PacketBuffer(in);
        int length = pBuf.readVarInt();
        return in.readBytes(length);
    }

    @Override
    public void writeTo(ByteBuf out, ByteBuf t) {
        PacketBuffer pBuf = new PacketBuffer(out);
        if (t != null) {
            pBuf.writeVarInt(t.readableBytes());
            out.writeBytes(t.readBytes(t.readableBytes()));
        } else {
            pBuf.writeVarInt(0);
        }
    }
}
