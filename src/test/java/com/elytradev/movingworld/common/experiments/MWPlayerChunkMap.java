package com.elytradev.movingworld.common.experiments;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;

/**
 * Created by darkevilmac on 2/23/2017.
 */
public class MWPlayerChunkMap extends PlayerChunkMap {
    public MWPlayerChunkMap(WorldServer serverWorld) {
        super(serverWorld);
    }

    @Override
    public PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ) {
        return super.getOrCreateEntry(chunkX, chunkZ);
    }
}
