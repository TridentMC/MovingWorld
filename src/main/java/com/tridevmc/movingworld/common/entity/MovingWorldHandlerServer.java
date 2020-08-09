package com.tridevmc.movingworld.common.entity;

import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.CompressedChunkData;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import com.tridevmc.movingworld.common.network.message.MovingWorldBlockChangeMessage;
import com.tridevmc.movingworld.common.network.message.MovingWorldTileChangeMessage;
import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;

public abstract class MovingWorldHandlerServer extends MovingWorldHandlerCommon {
    protected boolean firstChunkUpdate;

    public MovingWorldHandlerServer(EntityMovingWorld entitymovingWorld) {
        super(entitymovingWorld);
        firstChunkUpdate = true;
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        return false;
    }

    private MobileChunkServer getMobileChunkServer() {
        if (this.getMovingWorld() != null && this.getMovingWorld().getMobileChunk() != null && this.getMovingWorld().getMobileChunk().side() == LogicalSide.SERVER)
            return (MobileChunkServer) this.getMovingWorld().getMobileChunk();
        else
            return null;
    }

    @Override
    public void onChunkUpdate() {
        super.onChunkUpdate();
        if (getMobileChunkServer() != null) {
            MobileChunkServer mobileChunkServer = getMobileChunkServer();
            if (!firstChunkUpdate) {
                if (!mobileChunkServer.getBlockQueue().isEmpty()) {
                    new MovingWorldBlockChangeMessage(getMovingWorld(), new CompressedChunkData(mobileChunkServer, false)).sendToAllTracking(getMovingWorld());
                    MovingWorldMod.LOG.debug("MobileChunk block change detected, sending packet to all players watching " + getMovingWorld().toString());
                }
                if (!mobileChunkServer.getTileQueue().isEmpty()) {
                    new MovingWorldTileChangeMessage(getMovingWorld(), false).sendToAllTracking(getMovingWorld());
                    MovingWorldMod.LOG.debug("MobileChunk tile change detected, sending packet to all players watching " + getMovingWorld().toString());
                }
            }
            mobileChunkServer.getTileQueue().clear();
            mobileChunkServer.getBlockQueue().clear();
        }
        firstChunkUpdate = false;
    }
}

