package io.github.elytra.movingworld.common.event;

import io.github.elytra.movingworld.common.chunk.LocatedBlock;
import net.minecraftforge.fml.common.eventhandler.Event;

public class DisassembleBlockEvent extends Event {

    public LocatedBlock block;

    public DisassembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}
