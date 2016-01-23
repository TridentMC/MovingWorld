package darkevilmac.movingworld.common.core;

import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import java.io.File;

/**
 * Created by DarkEvilMac on 1/23/2016.
 */

public class MovingWorldAnvilChunkLoader extends AnvilChunkLoader {
    public MovingWorldAnvilChunkLoader(File chunkSaveLocationIn) {
        super(chunkSaveLocationIn);
    }
}
