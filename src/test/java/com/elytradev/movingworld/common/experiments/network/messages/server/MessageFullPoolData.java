package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Initializes all regionpools for the client this is sent to.
 */
@ReceivedOn(Side.CLIENT)
public class MessageFullPoolData extends Message {

    public NBTTagCompound data;

    public MessageFullPoolData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageFullPoolData(NBTTagCompound data) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.data = data;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (data != null) {
            RegionPool.readAllFromCompound(data);
        }
    }
}
