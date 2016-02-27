package darkevilmac.movingworld.client.handler;

import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraftforge.event.world.ChunkEvent;

public class ClientChunkIOHandler {

    public void onChunkLoad(ChunkEvent.Load e) {
        if (e.world != null && !(e.world instanceof IMovingWorld) && e.world.isRemote) {

        }
    }

    public void onChunkUnload(ChunkEvent.Unload e) {
        if (e.world != null && !(e.world instanceof IMovingWorld) && e.world.isRemote) {

        }
    }

}
