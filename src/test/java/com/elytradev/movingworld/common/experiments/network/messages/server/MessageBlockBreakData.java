package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 3/15/2017.
 */
@ReceivedOn(Side.CLIENT)
public class MessageBlockBreakData extends Message {

    public MessageBlockBreakData(NetworkContext ctx) {
        super(ctx);
    }

    @Override
    protected void handle(EntityPlayer sender) {

    }
}
