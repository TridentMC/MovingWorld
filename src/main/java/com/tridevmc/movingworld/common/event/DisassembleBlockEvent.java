package com.tridevmc.movingworld.common.event;

import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraftforge.eventbus.api.Event;

public class DisassembleBlockEvent extends Event {

    public final EntityMovingWorld movingWorld;
    public final LocatedBlock block;

    public DisassembleBlockEvent(EntityMovingWorld movingWorld, LocatedBlock block) {
        this.movingWorld = movingWorld;
        this.block = block;
    }
}
