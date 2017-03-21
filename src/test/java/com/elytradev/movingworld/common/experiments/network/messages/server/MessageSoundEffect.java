package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.marshallers.SoundEventMarshaller;
import com.elytradev.movingworld.common.experiments.region.MobileRegion;
import com.elytradev.movingworld.common.experiments.region.RegionPool;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.Validate;

/**
 * Created by darkevilmac on 3/15/2017.
 */
@ReceivedOn(Side.CLIENT)

public class MessageSoundEffect extends Message {

    @MarshalledAs("int")
    private int dimension;

    @MarshalledAs(SoundEventMarshaller.MARSHALLER_NAME)
    private SoundEvent sound;
    private SoundCategory category;
    @MarshalledAs("int")
    private int posX, posY, posZ;
    @MarshalledAs("float")
    private float soundVolume, soundPitch;

    public MessageSoundEffect(NetworkContext ctx) {
        super(ctx);
    }

    public MessageSoundEffect(int dimension, SoundEvent soundIn, SoundCategory categoryIn, double xIn, double yIn, double zIn, float volumeIn, float pitchIn) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.dimension = dimension;

        Validate.notNull(soundIn, "sound");
        this.sound = soundIn;
        this.category = categoryIn;
        this.posX = (int) xIn;
        this.posY = (int) yIn;
        this.posZ = (int) zIn;
        this.soundVolume = volumeIn;
        this.soundPitch = pitchIn;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        RegionPool pool = RegionPool.getPool(dimension, false);
        ChunkPos chunkPos = new ChunkPos(new BlockPos(posX, posY, posZ));
        MobileRegion region = pool.regions.get(chunkPos);
        if (pool == null || region == null) {
            MovingWorldExperimentsMod.logger.warn("Failed to transform position for effect, " + this.toString());
            return;
        }

        Vec3d transformedPosition = region.convertRegionPosToRealWorld(new Vec3d(posX, posY, posZ));

        Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player, transformedPosition.xCoord,
                transformedPosition.yCoord, transformedPosition.zCoord, sound, category, soundVolume, soundPitch);
    }

    @Override
    public String toString() {
        return "MessageSoundEffect{" +
                "dimension=" + dimension +
                ", sound=" + sound +
                ", category=" + category +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                ", soundVolume=" + soundVolume +
                ", soundPitch=" + soundPitch +
                '}';
    }
}
