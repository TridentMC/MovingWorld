package com.elytradev.movingworld.common.event;

import net.minecraftforge.fml.common.eventhandler.Event;

import com.elytradev.movingworld.common.chunk.LocatedBlock;

public class DisassembleBlockEvent extends Event {

    public LocatedBlock block;

    public DisassembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}
