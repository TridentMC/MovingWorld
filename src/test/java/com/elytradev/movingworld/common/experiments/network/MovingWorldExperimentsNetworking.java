package com.elytradev.movingworld.common.experiments.network;

import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.messages.client.MessageRequestRegionData;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessagePoolChange;
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
        networkContext.register(MessageRequestRegionData.class);

        // Register packets bound to client.
        networkContext.register(MessageRegionData.class);
        networkContext.register(MessagePoolChange.class);
    }

}
