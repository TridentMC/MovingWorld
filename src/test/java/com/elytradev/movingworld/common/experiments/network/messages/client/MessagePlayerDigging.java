package com.elytradev.movingworld.common.experiments.network.messages.client;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.interact.MWPlayerInteractionManager;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageBlockChange;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
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
        EntityPlayerMP player = (EntityPlayerMP) senderIn;
        WorldServer worldserver = (WorldServer) regionInteractedWith.getParentWorld();

        if (!MWPlayerInteractionManager.MANAGERS.containsKey(player)) {
            MWPlayerInteractionManager.MANAGERS.put(player, new MWPlayerInteractionManager(regionInteractedWith, player));
        } else {
            MWPlayerInteractionManager.MANAGERS.get(player).setRegionEntity(regionInteractedWith);
        }

        MWPlayerInteractionManager interactionManager = MWPlayerInteractionManager.MANAGERS.get(player);


        player.markPlayerActive();
        switch (action) {
            case SWAP_HELD_ITEMS:
                if (!player.isSpectator()) {
                    ItemStack itemstack = player.getHeldItem(EnumHand.OFF_HAND);
                    player.setHeldItem(EnumHand.OFF_HAND, player.getHeldItem(EnumHand.MAIN_HAND));
                    player.setHeldItem(EnumHand.MAIN_HAND, itemstack);
                }

                return;
            case DROP_ITEM:
                if (!player.isSpectator()) {
                    player.dropItem(false);
                }

                return;
            case DROP_ALL_ITEMS:
                if (!player.isSpectator()) {
                    player.dropItem(true);
                }

                return;
            case RELEASE_USE_ITEM:
                player.stopActiveHand();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                BlockPos realWorldBlockPosition = regionInteractedWith.region.convertRegionPosToRealWorld(position);
                double d0 = player.posX - ((double) realWorldBlockPosition.getX() + 0.5D);
                double d1 = player.posY - ((double) realWorldBlockPosition.getY() + 0.5D) + 1.5D;
                double d2 = player.posZ - ((double) realWorldBlockPosition.getZ() + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                double dist = player.interactionManager.getBlockReachDistance() + 1;
                dist *= dist;

                if (d3 > dist) {
                    return;
                } else if (worldserver.isOutsideBuildHeight(position)) {
                    return;
                } else {
                    if (action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        if (!worldserver.getMinecraftServer().isBlockProtected(worldserver, position, player) && worldserver.getWorldBorder().contains(position)) {
                            interactionManager.onBlockClicked(position, facing);
                        } else {
                            new MessageBlockChange(worldserver, position).sendTo(player);
                        }
                    } else {
                        if (action == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                            interactionManager.blockRemoving(position);
                        } else if (action == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                            interactionManager.cancelDestroyingBlock();
                        }

                        if (worldserver.getBlockState(position).getMaterial() != Material.AIR) {
                            new MessageBlockChange(worldserver, position).sendTo(player);
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }
}
