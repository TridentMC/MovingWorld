package com.elytradev.movingworld.common.experiments.network.marshallers;

import com.elytradev.concrete.Marshaller;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created by darkevilmac on 3/16/2017.
 */
public class SoundEventMarshaller implements Marshaller<SoundEvent> {

    public static final String MARSHALLER_NAME = "com.elytradev.movingworld.common.experiments.network.marshallers.SoundEventMarshaller";
    public static final SoundEventMarshaller INSTANCE = new SoundEventMarshaller();

    @Override
    public SoundEvent unmarshal(ByteBuf in) {
        return SoundEvent.REGISTRY.getObjectById(ByteBufUtils.readVarInt(in, 5));
    }

    @Override
    public void marshal(ByteBuf out, SoundEvent soundEvent) {
        ByteBufUtils.writeVarInt(out, SoundEvent.REGISTRY.getIDForObject(soundEvent), 5);
    }

}
