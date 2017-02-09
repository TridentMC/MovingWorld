package io.github.elytra.movingworld.common.network;

import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.network.message.*;

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
