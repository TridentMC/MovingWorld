package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Sends data about a specific pool when a player changes dimensions, used to make sure it's up to date.
 */
@ReceivedOn(Side.CLIENT)
public class MessageDimensionPoolData extends Message {

    @MarshalledAs("u16")
    public int dimension;
    public NBTTagCompound poolData;

    public MessageDimensionPoolData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageDimensionPoolData(int dimension, NBTTagCompound poolData) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.dimension = dimension;
        this.poolData = poolData;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (poolData != null) {
            RegionPool.getPool(dimension, true).readPoolFromCompound(poolData);
        }
    }
}
