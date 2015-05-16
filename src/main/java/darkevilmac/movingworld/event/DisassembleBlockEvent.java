package darkevilmac.movingworld.event;

import darkevilmac.movingworld.chunk.LocatedBlock;
import net.minecraftforge.fml.common.eventhandler.Event;

public class DisassembleBlockEvent extends Event {

    public LocatedBlock block;

    public DisassembleBlockEvent(LocatedBlock block) {
        this.block = block;
    }

}
