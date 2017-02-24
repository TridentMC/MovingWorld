package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.BlockData;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.ChunkData;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.marshallers.ClientEntityMarshaller;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

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

            List<Chunk> preppedChunks = new ArrayList<>();
            for (BlockData blockData : data.getBlockData()) {
                Chunk cAtPos = regionEntity.getParentWorld().getChunkFromBlockCoords(blockData.getPos());
                if (!preppedChunks.contains(cAtPos)) {
                    ((WorldClient) regionEntity.getParentWorld()).doPreChunk(cAtPos.xPosition, cAtPos.zPosition, true);
                    preppedChunks.add(cAtPos);
                }

                IBlockState res = cAtPos.setBlockState(blockData.getPos(), blockData.getState());

                System.out.println(res + " Set state to " + regionEntity.getParentWorld().getBlockState(blockData.getPos()) + " expected " + blockData.getState());
            }
        }
    }
}
