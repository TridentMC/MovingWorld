package com.elytradev.movingworld.common.experiments.network.marshallers;

import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import com.elytradev.concrete.Marshaller;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by darkevilmac on 2/22/2017.
 */
public class ClientEntityMarshaller implements Marshaller<Entity> {

    public static final String MARSHALLER_NAME = "com.elytradev.movingworld.common.experiments.network.marshallers.ClientEntityMarshaller";
    public static final ClientEntityMarshaller INSTANCE = new ClientEntityMarshaller();

    @Override
    public Entity unmarshal(ByteBuf in) {
        if (in.readBoolean()) {
            int dimID = in.readInt();
            World world = DimensionManager.getWorld(dimID);
            try {
                World mcWorld = Minecraft.getMinecraft().world;
                if (mcWorld.provider.getDimension() == dimID) {
                    world = mcWorld;
                }
            } catch (Exception e) {

            }

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
