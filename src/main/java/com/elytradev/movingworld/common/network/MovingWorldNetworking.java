package com.elytradev.movingworld.common.network;

import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.network.message.*;

public class MovingWorldNetworking {

    public static NetworkContext NETWORK;

    public static void setupNetwork() {
        //Init net code with builder.

        MovingWorldMod.LOG.info("Setting up network...");
        MovingWorldNetworking.NETWORK = registerPackets();
        MovingWorldMod.LOG.info("Setup network! " + MovingWorldNetworking.NETWORK.toString());
    }

    private static NetworkContext registerPackets() {
        NetworkContext context = NetworkContext.forChannel("MovingWorld");

        context.register(FarInteractMessage.class);
        context.register(MovingWorldBlockChangeMessage.class);
        context.register(MovingWorldTileChangeMessage.class);
        context.register(MovingWorldDataRequestMessage.class);
        context.register(MovingWorldClientActionMessage.class);

        return context;
    }
}
