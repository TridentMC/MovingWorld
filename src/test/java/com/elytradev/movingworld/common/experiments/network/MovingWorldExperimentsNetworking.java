package com.elytradev.movingworld.common.experiments.network;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageRequestData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageBlockData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageDimensionPoolData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageFullPoolData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageRegionData;
import io.github.elytra.concrete.NetworkContext;

/**
 * Stores networking information
 */
public class MovingWorldExperimentsNetworking {

    public static final NetworkContext networkContext = NetworkContext.forChannel(MovingWorldExperimentsMod.NETWORK_CHANNEL_NAME);

    public static void init() {
        // Register packets bound to server.
        networkContext.register(MessageRequestData.class);

        // Register packets bound to client.
        networkContext.register(MessageRegionData.class);
        networkContext.register(MessageFullPoolData.class);
        networkContext.register(MessageDimensionPoolData.class);
        networkContext.register(MessageBlockData.class);
    }

}
