package com.elytradev.movingworld.common.entity;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.CompressedChunkData;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import com.elytradev.movingworld.common.network.message.MovingWorldBlockChangeMessage;
import com.elytradev.movingworld.common.network.message.MovingWorldTileChangeMessage;
import com.elytradev.movingworld.common.tile.TileMovingMarkingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;

public abstract class MovingWorldHandlerServer extends MovingWorldHandlerCommon {
    protected boolean firstChunkUpdate;

    public MovingWorldHandlerServer(EntityMovingWorld entitymovingWorld) {
        super(entitymovingWorld);
        firstChunkUpdate = true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
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
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    NBTTagList list = new NBTTagList();
                    for (BlockPos tilePosition : mobileChunkServer.getTileQueue()) {
                        NBTTagCompound nbt = new NBTTagCompound();
                        if (mobileChunkServer.getTileEntity(tilePosition) == null)
                            continue;

                        TileEntity te = mobileChunkServer.getTileEntity(tilePosition);
                        if (te instanceof TileMovingMarkingBlock) {
                            ((TileMovingMarkingBlock) te).writeNBTForSending(nbt);
                        } else {
                            te.write(nbt);
                        }
                        list.add(nbt);
                    }
                    tagCompound.put("list", list);

                    new MovingWorldTileChangeMessage(getMovingWorld(), tagCompound).sendToAllTracking(getMovingWorld());
                    MovingWorldMod.LOG.debug("MobileChunk tile change detected, sending packet to all players watching " + getMovingWorld().toString());
                }
            }
            mobileChunkServer.getTileQueue().clear();
            mobileChunkServer.getBlockQueue().clear();
        }
        firstChunkUpdate = false;
    }
}

