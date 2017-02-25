package com.elytradev.movingworld.common.experiments;

import com.elytradev.concrete.Message;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageChunkData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Created by darkevilmac on 2/23/2017.
 */
public class MWPlayerChunkMapEntry extends PlayerChunkMapEntry {
    public MWPlayerChunkMapEntry(PlayerChunkMap mapIn, int chunkX, int chunkZ) {
        super(mapIn, chunkX, chunkZ);
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
    public void sendPacket(Packet<?> packetIn) {
        Message converted = convertVanillaPacketToMWMessage(packetIn);

        if (converted != null) {
            for (int i = 0; i < this.players.size(); ++i) {
                converted.sendTo(this.players.get(i));
            }
        } else {
            super.sendPacket(packetIn);
        }
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void sendBlockEntity(@Nullable TileEntity be) {
        super.sendBlockEntity(be);
    }

    @Override
    public boolean isSentToPlayers() {
        return super.isSentToPlayers();
    }

    public Message convertVanillaPacketToMWMessage(Packet<?> packet) {
        //TODO: Make this do what it says on the tin.

        Message convertedMessage = null;

        if (Objects.equals(packet.getClass(), SPacketChunkData.class)) {

        } else if (Objects.equals(packet.getClass(), SPacketBlockChange.class)) {

        } else if (Objects.equals(packet.getClass(), SPacketMultiBlockChange.class)) {

        } else if (Objects.equals(packet.getClass(), SPacketUpdateTileEntity.class)) {

        }

        return convertedMessage;
    }
}
