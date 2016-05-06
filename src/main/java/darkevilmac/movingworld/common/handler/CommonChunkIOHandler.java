package darkevilmac.movingworld.common.handler;

import darkevilmac.movingworld.asm.mixin.world.MixinWorldServer;
import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.ChunkEvent;

public class CommonChunkIOHandler {

    public void onChunkLoad(ChunkEvent.Load e) {
        if (e.getWorld() != null && !(e.getWorld() instanceof IMovingWorld)) {
            if (!e.getWorld().isRemote) {
                MixinWorldServer worldServer = (MixinWorldServer) (Object) e.getWorld();
                if (!worldServer.getMovingWorlds().isEmpty()) {
                    BlockPos chunkShifted = new BlockPos(
                            e.getChunk().xPosition << 4, 0,
                            e.getChunk().zPosition << 4
                    );
                    for (IMovingWorld movingWorld : worldServer.getMovingWorlds()) {
                        if (movingWorld.isInRangeToLoad(new Vec3d(chunkShifted.getX(),
                                movingWorld.worldTranslation().yCoord, chunkShifted.getZ()))) {

                        }
                    }
                }
            }
        }
    }

    public void onChunkUnload(ChunkEvent.Unload e) {
        if (e.getWorld() != null && !(e.getWorld() instanceof IMovingWorld)) {

        }
    }

}
