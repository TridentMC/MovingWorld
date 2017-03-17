package com.elytradev.movingworld.common.experiments.world;

import com.elytradev.concrete.Message;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.MovingWorldInitHandler;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageEffect;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageSoundEffect;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

/**
 * Created by darkevilmac on 3/15/2017.
 */
public class MWServerWorldEventHandler extends ServerWorldEventHandler {

    private MinecraftServer mcServer;
    private WorldServer worldServer;

    public MWServerWorldEventHandler(MinecraftServer mcServerIn, WorldServer worldServerIn) {
        super(mcServerIn, worldServerIn);
        this.mcServer = mcServerIn;
        this.worldServer = worldServerIn;
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        sendToAllNearExcept(new MessageSoundEffect(worldServer.provider.getDimension(), soundIn, category, x, y, z, volume, pitch),
                x, y, z, volume > 1.0F ? (double) (16.0F * volume) : 16.0D, player);
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos pos, int data) {
        sendToAllNearExcept(new MessageEffect(data, type, worldServer.provider.getDimension(), pos, false), pos.getX(), pos.getY(), pos.getZ(), 64D, player);
    }

    @Override
    public void broadcastSound(int type, BlockPos pos, int data) {
        new MessageEffect(data, type, worldServer.provider.getDimension(), pos, true).sendToEveryone();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        super.sendBlockBreakProgress(breakerId, pos, progress);
    }

    private void sendToAllNearExcept(Message m, double x, double y, double z, double radius, EntityPlayer skip) {
        for (int i = 0; i < mcServer.getPlayerList().getPlayers().size(); ++i) {
            EntityPlayerMP sendTo = mcServer.getPlayerList().getPlayers().get(i);

            if (sendTo != skip && sendTo.dimension == MovingWorldInitHandler.registeredDimensions.inverse().get(worldServer.provider.getDimension())) {
                //Transform position for checking range...
                Vec3d playerPos = new Vec3d(sendTo.posX, sendTo.posY, sendTo.posZ);

                RegionPool pool = RegionPool.getPool(worldServer.provider.getDimension(), false);
                MobileRegion region = pool.regions.get(new ChunkPos(new BlockPos(x, y, z)));
                if (pool == null) {
                    MovingWorldExperimentsMod.logger.warn("Failed to locate pool to send effect for dimension " + this.worldServer.provider.getDimension());
                    return;
                }

                Vec3d transformedPosition = region.convertRealWorldPosToRegion(playerPos);

                double d0 = x - transformedPosition.xCoord;
                double d1 = y - transformedPosition.yCoord;
                double d2 = z - transformedPosition.zCoord;

                if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) {
                    m.sendTo(sendTo);
                }
            }
        }
    }
}
