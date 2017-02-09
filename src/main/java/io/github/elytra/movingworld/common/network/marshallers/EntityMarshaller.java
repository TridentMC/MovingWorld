package io.github.elytra.movingworld.common.network.marshallers;

import io.github.elytra.concrete.Marshaller;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class EntityMarshaller implements Marshaller<Entity> {

    public static final String MARSHALLER_NAME = "io.github.elytra.movingworld.common.network.marshallers.EntityMarshaller";
    public static final EntityMarshaller INSTANCE = new EntityMarshaller();

    @Override
    public Entity unmarshal(ByteBuf in) {
        if (in.readBoolean()) {
            World world = DimensionManager.getWorld(in.readInt());
            return world.getEntityByID(in.readInt());
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
