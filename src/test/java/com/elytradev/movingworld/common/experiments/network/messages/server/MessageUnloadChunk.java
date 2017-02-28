package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 2/25/2017.
 */
@ReceivedOn(Side.CLIENT)
public class MessageUnloadChunk extends Message {

    @MarshalledAs("varint")
    public int x;
    @MarshalledAs("varint")
    public int z;
    @MarshalledAs("varint")
    public int dimension;

    public MessageUnloadChunk(NetworkContext ctx) {
        super(ctx);
    }

    public MessageUnloadChunk(int x, int z, int dimension) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        WorldClient worldClient = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(dimension);

        //worldClient.doPreChunk(x, z, false);
    }
}
