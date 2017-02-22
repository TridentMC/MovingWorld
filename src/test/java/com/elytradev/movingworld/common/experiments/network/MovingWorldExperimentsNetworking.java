package com.elytradev.movingworld.common.experiments.network;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageRequestData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageBlockData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageDimensionPoolData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageFullPoolData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageRegionData;
import io.github.elytra.concrete.NetworkContext;

/**
 * Created by darkevilmac on 2/18/2017.
 */
public class MovingWorldExperimentsNetworking {

    public static NetworkContext networkContext;

    public static void init() {
        networkContext = NetworkContext.forChannel(MovingWorldExperimentsMod.MOD_ID);

        // Register packets bound to server.
        networkContext.register(MessageRequestData.class);

        // Register packets bound to client.
        networkContext.register(MessageRegionData.class);
        networkContext.register(MessageFullPoolData.class);
        networkContext.register(MessageDimensionPoolData.class);
        networkContext.register(MessageBlockData.class);
    }

}
