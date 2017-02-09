package io.github.elytra.movingworld.common.entity;

import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.chunk.ChunkIO;
import io.github.elytra.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import io.github.elytra.movingworld.common.network.message.MovingWorldBlockChangeMessage;
import io.github.elytra.movingworld.common.network.message.MovingWorldTileChangeMessage;
import io.github.elytra.movingworld.common.tile.TileMovingMarkingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

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
        if (this.getMovingWorld() != null && this.getMovingWorld().getMobileChunk() != null && this.getMovingWorld().getMobileChunk().side().isServer())
            return (MobileChunkServer) this.getMovingWorld().getMobileChunk();
        else
            return null;
    }

    @Override
    public void onChunkUpdate() {
        super.onChunkUpdate();
        if (getMobileChunkServer() != null) {
            if (!firstChunkUpdate) {
                if (!getMobileChunkServer().getBlockQueue().isEmpty()) {
                    new MovingWorldBlockChangeMessage(getMovingWorld(),
                            ChunkIO.writeCompressed(getMovingWorld().getMobileChunk(), getMobileChunkServer().getBlockQueue())).sendToAllWatching(getMovingWorld());

                    MovingWorldMod.LOG.debug("MobileChunk block change detected, sending packet to all players watching " + getMovingWorld().toString());
                }
                if (!getMobileChunkServer().getTileQueue().isEmpty()) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    NBTTagList list = new NBTTagList();
                    for (BlockPos tilePosition : getMobileChunkServer().getTileQueue()) {
                        NBTTagCompound nbt = new NBTTagCompound();
                        if (getMobileChunkServer().getTileEntity(tilePosition) == null)
                            continue;

                        TileEntity te = getMobileChunkServer().getTileEntity(tilePosition);
                        if (te instanceof TileMovingMarkingBlock) {
                            ((TileMovingMarkingBlock) te).writeNBTForSending(nbt);
                        } else {
                            te.writeToNBT(nbt);
                        }
                        list.appendTag(nbt);
                    }
                    tagCompound.setTag("list", list);

                    new MovingWorldTileChangeMessage(getMovingWorld(), tagCompound).sendToAllWatching(getMovingWorld());
                    MovingWorldMod.LOG.debug("MobileChunk tile change detected, sending packet to all players watching " + getMovingWorld().toString());
                }
            }
            getMobileChunkServer().getTileQueue().clear();
            getMobileChunkServer().getBlockQueue().clear();
        }
        firstChunkUpdate = false;
    }
}

