package com.elytradev.movingworld.common.experiments;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;

/**
 * Created by darkevilmac on 2/23/2017.
 */
public class MWPlayerChunkMapEntry extends PlayerChunkMapEntry {
    public MWPlayerChunkMapEntry(PlayerChunkMap mapIn, int chunkX, int chunkZ) {
        super(mapIn, chunkX, chunkZ);
    }
}
