package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.movingworld.common.experiments.BlockData;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.ChunkData;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.marshallers.ClientEntityMarshaller;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.CLIENT)
public class MessageBlockData extends Message {

    @MarshalledAs(ClientEntityMarshaller.MARSHALLER_NAME)
    public EntityMobileRegion regionEntity;
    public ChunkData data;

    public MessageBlockData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageBlockData(EntityMobileRegion regionEntity) {
        super(MovingWorldExperimentsNetworking.networkContext);

        this.regionEntity = regionEntity;
        this.data = new ChunkData(regionEntity.getParentWorld(), regionEntity.region);
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (regionEntity != null && data != null) {
            regionEntity.setupClientForData();

            for (BlockData blockData : data.getBlockData()) {
                Chunk chunkAtPos = regionEntity.getParentWorld().getChunkFromBlockCoords(blockData.getPos());
                chunkAtPos.setBlockState(blockData.getPos(), blockData.getState());
            }
        }
    }
}
