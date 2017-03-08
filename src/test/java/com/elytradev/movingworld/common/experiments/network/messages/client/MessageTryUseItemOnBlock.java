package com.elytradev.movingworld.common.experiments.network.messages.client;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.field.Optional;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 3/6/2017.
 */
@ReceivedOn(Side.SERVER)
public class MessageTryUseItemOnBlock extends Message {

    private transient Accessor<Vec3d> targetPos = Accessors.findField(NetHandlerPlayServer.class, "targetPos", "field_184362_y");

    @MarshalledAs("int")
    private int dimension;
    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    private EntityMobileRegion regionInteractedWith;

    private BlockPos regionPos;
    private EnumFacing placedBlockDirection;
    private EnumHand hand;
    @MarshalledAs("float")
    private float facingX, facingY, facingZ;

    public MessageTryUseItemOnBlock(NetworkContext ctx) {
        super(ctx);
    }

    public MessageTryUseItemOnBlock(EntityMobileRegion regionInteractedWith, BlockPos regionPos, EnumFacing placedBlockDirection, EnumHand hand, float facingX, float facingY, float facingZ) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.dimension = regionInteractedWith.region.dimension;
        this.regionInteractedWith = regionInteractedWith;
        this.regionPos = regionPos;
        this.placedBlockDirection = placedBlockDirection;
        this.hand = hand;
        this.facingX = facingX;
        this.facingY = facingY;
        this.facingZ = facingZ;
    }

    @Override
    protected void handle(EntityPlayer senderIn) {
        EntityPlayerMP player = (EntityPlayerMP) senderIn;
        WorldServer worldserver = (WorldServer) MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(dimension);
        BlockPos realWorldPos = regionInteractedWith.region.convertRegionPosToRealWorld(regionPos);
        ItemStack itemstack = player.getHeldItem(hand);
        player.markPlayerActive();

        if (regionPos.getY() < player.getServerWorld().getMinecraftServer().getBuildLimit() - 1 || placedBlockDirection != EnumFacing.UP && regionPos.getY() < player.getServerWorld().getMinecraftServer().getBuildLimit()) {
            double dist = player.interactionManager.getBlockReachDistance() + 3;
            dist *= dist;
            boolean withinRange = player.getDistanceSq((double) realWorldPos.getX() + 0.5D, (double) realWorldPos.getY() + 0.5D, (double) realWorldPos.getZ() + 0.5D) < dist;
            if (this.targetPos.get(player.connection) == null && withinRange && regionInteractedWith.region.isPosWithinBounds(regionPos)) {
                player.interactionManager.processRightClickBlock(player, worldserver, itemstack, hand, regionPos, placedBlockDirection, facingX, facingY, facingZ);
            }
        } else {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("build.tooHigh", new Object[]{Integer.valueOf(player.getServerWorld().getMinecraftServer().getBuildLimit())});
            textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
            player.connection.sendPacket(new SPacketChat(textcomponenttranslation, (byte) 2));
        }

        player.connection.sendPacket(new SPacketBlockChange(worldserver, regionPos));
        player.connection.sendPacket(new SPacketBlockChange(worldserver, regionPos.offset(placedBlockDirection)));
    }
}
