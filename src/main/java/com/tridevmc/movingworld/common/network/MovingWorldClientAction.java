package com.tridevmc.movingworld.common.network;

import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.network.message.MovingWorldClientActionMessage;

public enum MovingWorldClientAction {
    NONE, ALIGN, DISASSEMBLE, DISASSEMBLEWITHOVERWRITE;

    public void sendToServer(EntityMovingWorld target) {
        new MovingWorldClientActionMessage(target, this).sendToServer();
    }
}