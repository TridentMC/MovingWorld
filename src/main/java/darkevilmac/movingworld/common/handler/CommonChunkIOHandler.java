package darkevilmac.movingworld.common.handler;

import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraftforge.event.world.ChunkEvent;

public class CommonChunkIOHandler {

    public void onChunkLoad(ChunkEvent.Load e) {
        if (e.getWorld() != null && !(e.getWorld() instanceof IMovingWorld)) {

        }
    }

    public void onChunkUnload(ChunkEvent.Unload e) {
        if (e.getWorld() != null && !(e.getWorld() instanceof IMovingWorld)) {

        }
    }

}
