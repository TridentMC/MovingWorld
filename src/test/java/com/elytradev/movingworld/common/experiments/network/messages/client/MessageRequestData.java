package com.elytradev.movingworld.common.experiments.network.messages.client;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageChunkData;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 2/21/2017.
 */
@ReceivedOn(Side.SERVER)
public class MessageRequestData extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public EntityMobileRegion regionEntity;

    public MessageRequestData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageRequestData(EntityMobileRegion regionEntity) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.regionEntity = regionEntity;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (regionEntity != null) {
            // return to sender, address unknown.
            for (int cX = regionEntity.region.regionMin.chunkXPos; cX < regionEntity.region.regionMax.chunkXPos; cX++) {
                for (int cZ = regionEntity.region.regionMin.chunkZPos; cZ < regionEntity.region.regionMax.chunkZPos; cZ++) {
                    new MessageChunkData(regionEntity, regionEntity.getParentWorld().getChunkFromChunkCoords(cX, cZ), 65535).sendTo(sender);
                }
            }
        }
    }
}
