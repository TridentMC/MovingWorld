package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Used to send additional regions to clients when a new region is selected in a pool.
 */
@ReceivedOn(Side.CLIENT)
public class MessageRegionData extends Message {

    @MarshalledAs("varint")
    public int pool;
    public NBTTagCompound data;

    public MessageRegionData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageRegionData(int pool, NBTTagCompound data) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.pool = pool;
        this.data = data;
    }

    public MessageRegionData(MobileRegion region) {
        this(region.dimension, region.writeToCompound());
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (data != null) {
            RegionPool regionPool = RegionPool.getPool(pool, true);
            MobileRegion region = MobileRegion.getRegionFor(data);

            // This message happens when we assumed the pool wasn't present, if it's already there we don't need to add it.
            regionPool.addRegionIfNotPresent(region);
        }
    }
}
