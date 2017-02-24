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

    // just a copy of the same method found in the vanilla PlayerChunkMap.
    private static long getIndex(int p_187307_0_, int p_187307_1_) {
        return (long) p_187307_0_ + 2147483647L | (long) p_187307_1_ + 2147483647L << 32;
    }

    @Override
    public PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ) {
        long i = getIndex(chunkX, chunkZ);
        PlayerChunkMapEntry playerchunkmapentry = this.entryMap.get(i);

        if (playerchunkmapentry == null) {
            playerchunkmapentry = new MWPlayerChunkMapEntry(this, chunkX, chunkZ);
            this.entryMap.put(i, playerchunkmapentry);
            this.entries.add(playerchunkmapentry);

            if (playerchunkmapentry.getChunk() == null) {
                this.entriesWithoutChunks.add(playerchunkmapentry);
            }

            if (!playerchunkmapentry.sendToPlayers()) {
                this.pendingSendToPlayers.add(playerchunkmapentry);
            }
        }

        return playerchunkmapentry;
    }
}
