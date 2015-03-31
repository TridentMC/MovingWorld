package darkevilmac.movingworld.event;

import cpw.mods.fml.common.eventhandler.Event;
import darkevilmac.movingworld.chunk.LocatedBlock;

/**
 * Created by DarkEvilMac on 2/22/2015.
 */

public class AssembleBlockEvent extends Event {

    public LocatedBlock block;

    public AssembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}