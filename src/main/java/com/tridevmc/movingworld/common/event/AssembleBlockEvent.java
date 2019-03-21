package com.tridevmc.movingworld.common.event;

import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import net.minecraftforge.eventbus.api.Event;

public class AssembleBlockEvent extends Event {

    public LocatedBlock block;

    public AssembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}