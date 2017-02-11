package com.elytradev.movingworld.common.network.marshallers;

import io.github.elytra.concrete.Marshaller;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Code from Concrete that's not available in 0.0.6 builds.
 */
public class ByteBufMarshaller implements Marshaller<ByteBuf> {

    public static final String MARSHALLER_NAME = "com.elytradev.movingworld.common.network.marshallers.ByteBufMarshaller";
    public static final ByteBufMarshaller INSTANCE = new ByteBufMarshaller();

    @Override
    public ByteBuf unmarshal(ByteBuf in) {
        int length = ByteBufUtils.readVarInt(in, 5);

        return in.readBytes(length);
    }

    @Override
    public void marshal(ByteBuf out, ByteBuf t) {
        if (t != null) {
            ByteBufUtils.writeVarInt(out, t.readableBytes(), 5);
            out.writeBytes(t.readBytes(t.readableBytes()));
        } else {
            ByteBufUtils.writeVarInt(out, 0, 5);
        }
    }
}
