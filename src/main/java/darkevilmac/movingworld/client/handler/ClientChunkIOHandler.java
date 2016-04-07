package darkevilmac.movingworld.client.handler;

import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraftforge.event.world.ChunkEvent;

public class ClientChunkIOHandler {

    public void onChunkLoad(ChunkEvent.Load e) {
        if (e.getWorld() != null && !(e.getWorld() instanceof IMovingWorld) && e.getWorld().isRemote) {

        }
    }

    public void onChunkUnload(ChunkEvent.Unload e) {
        if (e.getWorld() != null && !(e.getWorld() instanceof IMovingWorld) && e.getWorld().isRemote) {

        }
    }

}
