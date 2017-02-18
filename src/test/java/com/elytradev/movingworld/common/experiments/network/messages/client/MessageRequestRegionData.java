package com.elytradev.movingworld.common.experiments.network.messages.client;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 2/18/2017.
 */
@ReceivedOn(Side.SERVER)
public class MessageRequestRegionData extends Message {
    public MessageRequestRegionData(NetworkContext ctx) {
        super(ctx);
    }

    @Override
    protected void handle(EntityPlayer sender) {

    }
}
