package com.elytradev.movingworld.common.network.marshallers;

import com.elytradev.concrete.network.Marshaller;
import com.elytradev.movingworld.MovingWorldMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityMarshaller implements Marshaller<Entity> {

    public static final String MARSHALLER_NAME = "com.elytradev.movingworld.common.network.marshallers.EntityMarshaller";
    public static final EntityMarshaller INSTANCE = new EntityMarshaller();

    @Override
    public Entity unmarshal(ByteBuf in) {
        if (in.readBoolean()) {
            int dimID = in.readInt();
            int entityID = in.readInt();
            World world = MovingWorldMod.PROXY.getWorld(dimID);
            return world.getEntityByID(entityID);
        } else {
            return null;
        }
    }

    @Override
    public void marshal(ByteBuf out, Entity entity) {
        if (entity != null) {
            out.writeBoolean(true);
            out.writeInt(entity.world.provider.getDimension());
            out.writeInt(entity.getEntityId());
        } else {
            out.writeBoolean(false);
        }
    }
}
