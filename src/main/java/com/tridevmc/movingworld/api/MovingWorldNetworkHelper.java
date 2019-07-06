package com.tridevmc.movingworld.api;

import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.network.message.MovingWorldDataRequestMessage;

public class MovingWorldNetworkHelper {

    public static void sendDataRequestMessage(EntityMovingWorld entity) {
        new MovingWorldDataRequestMessage(entity).sendToServer();
    }

}
