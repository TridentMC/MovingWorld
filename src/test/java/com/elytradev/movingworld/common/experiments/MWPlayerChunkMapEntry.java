package com.elytradev.movingworld.common.experiments;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageBlockChange;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageChunkData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Created by darkevilmac on 2/23/2017.
 */
public class MWPlayerChunkMapEntry extends PlayerChunkMapEntry {

    private Accessor<Boolean> loading;
    private Accessor<Runnable> loadedRunnable;


    public MWPlayerChunkMapEntry(PlayerChunkMap mapIn, int chunkX, int chunkZ) {
        super(mapIn, chunkX, chunkZ);

        loading = Accessors.findField(PlayerChunkMapEntry.class, "loading");
        loadedRunnable = Accessors.findField(PlayerChunkMapEntry.class, "loadedRunnable");
    }

    public void addPlayer(EntityPlayerMP player) {
        if (this.players.contains(player)) {
            LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", player, Integer.valueOf(this.pos.chunkXPos), Integer.valueOf(this.pos.chunkZPos));
        } else {
            if (this.players.isEmpty()) {
                this.lastUpdateInhabitedTime = this.playerChunkMap.getWorldServer().getTotalWorldTime();
            }

            this.players.add(player);

            if (this.sentToPlayers) {
                this.sendToPlayer(player);
            }
        }
    }

    public void removePlayer(EntityPlayerMP player) {
        if (this.players.contains(player)) {
            // If we haven't loaded yet don't load the chunk just so we can clean it up
            if (this.chunk == null) {
                this.players.remove(player);

                if (this.players.isEmpty()) {
                    if (this.loading.get(this))
                        net.minecraftforge.common.chunkio.ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable.get(this));
                    this.playerChunkMap.removeEntry(this);
                }

                return;
            }

            if (this.sentToPlayers) {
                //TODO: MessageUnloadChunk
                player.connection.sendPacket(new SPacketUnloadChunk(this.pos.chunkXPos, this.pos.chunkZPos));
            }

            this.players.remove(player);

            if (this.players.isEmpty()) {
                this.playerChunkMap.removeEntry(this);
            }
        }
    }

    @Override
    public boolean sendToPlayers() {
        if (this.sentToPlayers) {
            return true;
        } else if (this.chunk == null) {
            return false;
        } else if (!this.chunk.isPopulated()) {
            return false;
        } else {
            this.changes = 0;
            this.changedSectionFilter = 0;
            this.sentToPlayers = true;

            MessageChunkData msg = new MessageChunkData(getChunk().getWorld().provider.getDimension(), this.chunk, 65535);

            for (EntityPlayerMP entityplayermp : this.players) {
                msg.sendTo(entityplayermp);
            }

            return true;
        }
    }

    @Override
    public void sendToPlayer(EntityPlayerMP player) {
        if (this.sentToPlayers) {
            MessageChunkData msg = new MessageChunkData(getChunk().getWorld().provider.getDimension(), this.chunk, 65535);
            msg.sendTo(player);
        }
    }


    @Override
    public void update() {
        if (this.sentToPlayers && this.chunk != null) {
            if (this.changes != 0) {
                if (this.changes == 1) {
                    int i = (this.changedBlocks[0] >> 12 & 15) + this.pos.chunkXPos * 16;
                    int j = this.changedBlocks[0] & 255;
                    int k = (this.changedBlocks[0] >> 8 & 15) + this.pos.chunkZPos * 16;
                    BlockPos blockpos = new BlockPos(i, j, k);
                    this.sendToAllPlayers(new MessageBlockChange(this.playerChunkMap.getWorldServer(), blockpos));
                    net.minecraft.block.state.IBlockState state = this.playerChunkMap.getWorldServer().getBlockState(blockpos);

                    if (state.getBlock().hasTileEntity(state)) {
                        this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos));
                    }
                } else if (this.changes >= net.minecraftforge.common.ForgeModContainer.clumpingThreshold) {
                    this.sendToAllPlayers(new MessageChunkData(this.chunk, this.changedSectionFilter));
                } else {
                    // TODO: MessageMultiBlockChange this.sendToAllPlayers(new SPacketMultiBlockChange(this.changes, this.changedBlocks, this.chunk));
                    for (int l = 0; l < this.changes; ++l) {
                        int i1 = (this.changedBlocks[l] >> 12 & 15) + this.pos.chunkXPos * 16;
                        int j1 = this.changedBlocks[l] & 255;
                        int k1 = (this.changedBlocks[l] >> 8 & 15) + this.pos.chunkZPos * 16;
                        BlockPos blockpos1 = new BlockPos(i1, j1, k1);
                        net.minecraft.block.state.IBlockState state = this.playerChunkMap.getWorldServer().getBlockState(blockpos1);

                        if (state.getBlock().hasTileEntity(state)) {
                            this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos1));
                        }
                    }
                }

                this.changes = 0;
                this.changedSectionFilter = 0;
            }
        }
    }

    @Override
    public void sendBlockEntity(@Nullable TileEntity be) {
        if (be != null) {
            // TODO: MessageUpdateTileEntity
            SPacketUpdateTileEntity spacketupdatetileentity = be.getUpdatePacket();

            if (spacketupdatetileentity != null) {
                this.sendPacket(spacketupdatetileentity);
            }
        }
    }

    public void sendToAllPlayers(Message message) {
        if (this.sentToPlayers) {
            for (int i = 0; i < this.players.size(); ++i) {
                message.sendTo(this.players.get(i));
            }
        }
    }

}
