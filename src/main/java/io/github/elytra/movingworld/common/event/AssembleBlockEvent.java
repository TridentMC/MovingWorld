package io.github.elytra.movingworld.common.event;

import net.minecraftforge.fml.common.eventhandler.Event;

import io.github.elytra.movingworld.common.chunk.LocatedBlock;

/**
 * Created by DarkEvilMac on 2/22/2015.
 */

public class AssembleBlockEvent extends Event {

    public LocatedBlock block;

    public AssembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}