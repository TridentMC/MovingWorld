package com.tridevmc.movingworld.common.network.message;


import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Sends tile entity data in a MobileChunk to clients.
 */
@RegisteredMessage(channel = "movingworld", destination = LogicalSide.CLIENT)
public class MovingWorldTileChangeMessage extends Message {

    public EntityMovingWorld movingWorld;
    public CompoundNBT tileData;

    public MovingWorldTileChangeMessage() {
        super();
    }

    public MovingWorldTileChangeMessage(EntityMovingWorld movingWorld, boolean sendAll) {
        super();
        this.movingWorld = movingWorld;
        this.tileData = new CompoundNBT();
        ListNBT tiles = new ListNBT();
        if (sendAll) {
            movingWorld.getMobileChunk().chunkTileEntityMap.forEach((p, t) -> {
                CompoundNBT tileCompound = new CompoundNBT();
                tileCompound.putIntArray("pos", new int[]{p.getX(), p.getY(), p.getZ()});
                tileCompound.putInt("state", Block.getStateId(movingWorld.getMobileChunk().getBlockState(p)));
                tileCompound.put("tile", t.write(new CompoundNBT()));
                tiles.add(tileCompound);
            });
        } else {
            movingWorld.getMobileChunk().getTileQueue()
                    .forEach(p -> {
                        TileEntity t = movingWorld.getMobileChunk().getTileEntity(p);
                        CompoundNBT tileCompound = new CompoundNBT();
                        tileCompound.putIntArray("pos", new int[]{p.getX(), p.getY(), p.getZ()});
                        tileCompound.putInt("state", Block.getStateId(movingWorld.getMobileChunk().getBlockState(p)));
                        tileCompound.put("tile", t != null ? t.write(new CompoundNBT()) : new CompoundNBT());
                        tiles.add(tileCompound);
                    });
        }
        this.tileData.put("tiles", tiles);
    }

    @Override
    public void handle(PlayerEntity sender) {
        if (movingWorld == null || tileData == null || !(movingWorld.getMobileChunk() instanceof MobileChunkClient))
            return;

        ListNBT list = tileData.getList("tiles", 10);
        for (INBT nbt : list) {
            CompoundNBT compound = (CompoundNBT) nbt;
            int[] posArray = compound.getIntArray("pos");
            int stateId = compound.getInt("state");
            CompoundNBT tileCompound = compound.getCompound("tile");
            BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
            BlockState state = Block.getStateById(stateId);
            try {
                TileEntity tile = this.movingWorld.getMobileChunk().getTileEntity(pos);
                if(tile != null)
                    tile.read(state, tileCompound);
            } catch (Exception e){
                MovingWorldMod.LOG.error("Failed to deserialize a tile that was sent over the wire for a MovingWorld entity", e);
            }
        }
        ((MobileChunkClient) movingWorld.getMobileChunk()).getRenderer().markDirty();
    }
}
