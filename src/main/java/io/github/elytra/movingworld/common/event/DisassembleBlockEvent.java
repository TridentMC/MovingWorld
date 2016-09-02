package io.github.elytra.movingworld.common.event;

import net.minecraftforge.fml.common.eventhandler.Event;

import io.github.elytra.movingworld.common.chunk.LocatedBlock;

public class DisassembleBlockEvent extends Event {

    public LocatedBlock block;

    public DisassembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}
