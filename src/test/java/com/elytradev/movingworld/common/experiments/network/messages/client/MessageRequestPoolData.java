package com.elytradev.movingworld.common.experiments.network.messages.client;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by darkevilmac on 2/21/2017.
 */
public class MessageRequestPoolData extends Message {
    public MessageRequestPoolData(NetworkContext ctx) {
        super(ctx);
    }

    @Override
    protected void handle(EntityPlayer sender) {

    }
}
