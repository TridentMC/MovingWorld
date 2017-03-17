package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Alternative packet for subworlds.
 *
 * @see net.minecraft.network.play.server.SPacketEffect
 */
@ReceivedOn(Side.CLIENT)
public class MessageEffect extends Message {

    @MarshalledAs("int")
    private int soundData, soundType, dimension;
    private BlockPos soundPos;
    private boolean serverWide;

    public MessageEffect(NetworkContext ctx) {
        super(ctx);
    }

    public MessageEffect(int soundData, int soundType, int dimension, BlockPos soundPos, boolean serverWide) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.soundData = soundData;
        this.soundType = soundType;
        this.dimension = dimension;
        this.soundPos = soundPos;
        this.serverWide = serverWide;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        RegionPool pool = RegionPool.getPool(dimension, false);
        MobileRegion region = pool.regions.get(new ChunkPos(soundPos));
        if (pool == null) {
            MovingWorldExperimentsMod.logger.warn("Failed to transform position for effect, " + this.toString());
            return;
        }

        BlockPos transformedPosition = region.convertRegionPosToRealWorld(soundPos);

        if (serverWide) {
            Minecraft.getMinecraft().world.playBroadcastSound(soundType, transformedPosition, soundData);
        } else {
            Minecraft.getMinecraft().world.playEvent(soundType, transformedPosition, soundData);
        }
    }

    @Override
    public String toString() {
        return "MessageEffect{" +
                "soundData=" + soundData +
                ", soundType=" + soundType +
                ", dimension=" + dimension +
                ", soundPos=" + soundPos +
                ", serverWide=" + serverWide +
                '}';
    }
}
