package com.elytradev.movingworld.common.network;

import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.network.message.MovingWorldClientActionMessage;

public enum MovingWorldClientAction {
    NONE, ALIGN, DISASSEMBLE, DISASSEMBLEWITHOVERWRITE;

    public void sendToServer(EntityMovingWorld target) {
        new MovingWorldClientActionMessage(target, this).sendToServer();
    }
}