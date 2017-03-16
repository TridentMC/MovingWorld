package com.elytradev.movingworld.common.experiments.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

/**
 * Created by darkevilmac on 3/15/2017.
 */
public class MWServerWorldEventHandler extends ServerWorldEventHandler {
    public MWServerWorldEventHandler(MinecraftServer mcServerIn, WorldServer worldServerIn) {
        super(mcServerIn, worldServerIn);
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        super.playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {
        super.playRecord(soundIn, pos);
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        super.playEvent(player, type, blockPosIn, data);
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        super.sendBlockBreakProgress(breakerId, pos, progress);
    }
}
