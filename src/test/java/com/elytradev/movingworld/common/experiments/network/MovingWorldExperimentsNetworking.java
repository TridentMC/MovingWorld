package com.elytradev.movingworld.common.experiments.network;

import com.elytradev.concrete.NetworkContext;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.messages.client.*;
import com.elytradev.movingworld.common.experiments.network.messages.server.*;

/**
 * Stores networking information
 */
public class MovingWorldExperimentsNetworking {

    public static final NetworkContext networkContext = NetworkContext.forChannel(MovingWorldExperimentsMod.NETWORK_CHANNEL_NAME);

    public static void init() {
        // Register packets bound to server.
        networkContext.register(MessageRequestData.class);
        networkContext.register(MessageTryUseItemOnBlock.class);
        networkContext.register(MessagePlayerDigging.class);

        // Register packets bound to client.
        networkContext.register(MessageBlockChange.class);
        networkContext.register(MessageMultiBlockChange.class);
        networkContext.register(MessageUnloadChunk.class);
        networkContext.register(MessageUpdateTile.class);
        networkContext.register(MessageRegionData.class);
        networkContext.register(MessageFullPoolData.class);
        networkContext.register(MessageDimensionPoolData.class);
        networkContext.register(MessageChunkData.class);
        networkContext.register(MessageOpenGui.class);
        networkContext.register(MessageSoundEffect.class);
        networkContext.register(MessageEffect.class);
        networkContext.register(MessageBlockBreakData.class);
    }

}
