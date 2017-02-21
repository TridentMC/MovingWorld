package com.elytradev.movingworld.common.experiments.network.messages.server;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Sends data about a specific pool when a player changes dimensions.
 */
@ReceivedOn(Side.CLIENT)
public class MessageDimensionPoolData extends Message {
    public MessageDimensionPoolData(NetworkContext ctx) {
        super(ctx);
    }

    @Override
    protected void handle(EntityPlayer sender) {

    }
}
