package com.elytradev.movingworld.common.experiments.network.messages.client;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;


@ReceivedOn(Side.SERVER)
public class MessagePlayerDigging extends Message {

    @MarshalledAs("int")
    private int dimension;
    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    private EntityMobileRegion regionInteractedWith;

    private BlockPos position;
    private EnumFacing facing;
    private CPacketPlayerDigging.Action action;

    public MessagePlayerDigging(NetworkContext ctx) {
        super(ctx);
    }

    public MessagePlayerDigging(EntityMobileRegion regionInteractedWith, CPacketPlayerDigging.Action action, BlockPos position, EnumFacing facing) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.dimension = regionInteractedWith.region.dimension;
        this.regionInteractedWith = regionInteractedWith;
        this.position = position;
        this.facing = facing;
        this.action = action;
    }

    @Override
    protected void handle(EntityPlayer senderIn) {
        EntityPlayerMP sender = (EntityPlayerMP) senderIn;
        WorldServer worldserver = (WorldServer) regionInteractedWith.getParentWorld();

        sender.markPlayerActive();
        switch (action) {
            case SWAP_HELD_ITEMS:

                if (!sender.isSpectator()) {
                    ItemStack itemstack = sender.getHeldItem(EnumHand.OFF_HAND);
                    sender.setHeldItem(EnumHand.OFF_HAND, sender.getHeldItem(EnumHand.MAIN_HAND));
                    sender.setHeldItem(EnumHand.MAIN_HAND, itemstack);
                }

                return;
            case DROP_ITEM:

                if (!sender.isSpectator()) {
                    sender.dropItem(false);
                }

                return;
            case DROP_ALL_ITEMS:

                if (!sender.isSpectator()) {
                    sender.dropItem(true);
                }

                return;
            case RELEASE_USE_ITEM:
                sender.stopActiveHand();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                double d0 = sender.posX - ((double) position.getX() + 0.5D);
                double d1 = sender.posY - ((double) position.getY() + 0.5D) + 1.5D;
                double d2 = sender.posZ - ((double) position.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                double dist = sender.interactionManager.getBlockReachDistance() + 1;
                dist *= dist;

                if (d3 > dist) {
                    return;
                } else if (worldserver.isOutsideBuildHeight(position)) {
                    return;
                } else {
                    if (action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        if (!worldserver.getMinecraftServer().isBlockProtected(worldserver, position, sender) && worldserver.getWorldBorder().contains(position)) {
                            sender.interactionManager.onBlockClicked(position, facing);
                        } else {
                            sender.connection.sendPacket(new SPacketBlockChange(worldserver, position));
                        }
                    } else {
                        if (action == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                            sender.interactionManager.blockRemoving(position);
                        } else if (action == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                            sender.interactionManager.cancelDestroyingBlock();
                        }

                        if (worldserver.getBlockState(position).getMaterial() != Material.AIR) {
                            sender.connection.sendPacket(new SPacketBlockChange(worldserver, position));
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }
}
