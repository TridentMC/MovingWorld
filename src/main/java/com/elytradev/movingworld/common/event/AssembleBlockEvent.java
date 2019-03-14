package com.elytradev.movingworld.common.event;

import com.elytradev.movingworld.common.chunk.LocatedBlock;
import net.minecraftforge.eventbus.api.Event;

public class AssembleBlockEvent extends Event {

    public LocatedBlock block;

    public AssembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}