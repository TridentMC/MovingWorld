package com.tridevmc.movingworld.common.network.marshallers;

import com.tridevmc.compound.network.marshallers.Marshaller;
import com.tridevmc.compound.network.marshallers.RegisteredMarshaller;
import com.tridevmc.movingworld.MovingWorldMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@RegisteredMarshaller(channel = "movingworld", ids = {"entity"}, acceptedTypes = {Entity.class})
public class EntityMarshaller extends Marshaller<Entity> {

    @Override
    public Entity readFrom(ByteBuf in) {
        if (in.readBoolean()) {
            PacketBuffer pBuf = new PacketBuffer(in);
            ResourceLocation registryName = new ResourceLocation(pBuf.readString());
            ResourceLocation registryValue = new ResourceLocation(pBuf.readString());
            int entityID = pBuf.readInt();
            World world = MovingWorldMod.PROXY.getWorld(RegistryKey.func_240903_a_(RegistryKey.func_240904_a_(registryName),registryValue));
            return world.getEntityByID(entityID);
        } else {
            return null;
        }
    }

    @Override
    public void writeTo(ByteBuf out, Entity entity) {
        if (entity != null) {
            out.writeBoolean(true);
            RegistryKey<World> worldRegistryKey = entity.world.func_234923_W_();
            ResourceLocation registryName = worldRegistryKey.getRegistryName();
            ResourceLocation registryValue = worldRegistryKey.func_240901_a_();
            PacketBuffer pBuf = new PacketBuffer(out);
            pBuf.writeString(registryName.toString());
            pBuf.writeString(registryValue.toString());
            pBuf.writeInt(entity.getEntityId());
        } else {
            out.writeBoolean(false);
        }
    }
}
