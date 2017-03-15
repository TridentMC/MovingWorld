package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 2/25/2017.
 */
@ReceivedOn(Side.CLIENT)
public class MessageUpdateTile extends Message {

    @MarshalledAs("varint")
    public int dimension;

    private BlockPos blockPos;
    @MarshalledAs("byte")
    private int tileEntityType;
    private NBTTagCompound nbt;

    public MessageUpdateTile(NetworkContext ctx) {
        super(ctx);
    }

    public MessageUpdateTile(BlockPos blockPos, int tileEntityType, NBTTagCompound nbt, TileEntity tileEntity) {
        super(MovingWorldExperimentsNetworking.networkContext);
        this.blockPos = blockPos;
        this.tileEntityType = tileEntityType;
        this.nbt = nbt;
        this.dimension = tileEntity.getWorld().provider.getDimension();
    }

    public MessageUpdateTile(SPacketUpdateTileEntity updatePacket, TileEntity tileEntity) {
        this(updatePacket.getPos(), updatePacket.getTileEntityType(), updatePacket.getNbtCompound(), tileEntity);
    }

    @Override
    protected void handle(EntityPlayer sender) {
        WorldClient worldClient = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(dimension);

        if (worldClient.isBlockLoaded(blockPos)) {
            TileEntity tileentity = worldClient.getTileEntity(blockPos);
            int i = tileEntityType;
            boolean flag = i == 2 && tileentity instanceof TileEntityCommandBlock;

            if (i == 1 && tileentity instanceof TileEntityMobSpawner || flag || i == 3 && tileentity instanceof TileEntityBeacon || i == 4 && tileentity instanceof TileEntitySkull || i == 5 && tileentity instanceof TileEntityFlowerPot || i == 6 && tileentity instanceof TileEntityBanner || i == 7 && tileentity instanceof TileEntityStructure || i == 8 && tileentity instanceof TileEntityEndGateway || i == 9 && tileentity instanceof TileEntitySign || i == 10 && tileentity instanceof TileEntityShulkerBox) {
                tileentity.readFromNBT(nbt);
            } else {
                if (tileentity != null) {
                    SPacketUpdateTileEntity sPacketUpdateTileEntity = new SPacketUpdateTileEntity(blockPos, tileEntityType, nbt);
                    tileentity.onDataPacket(worldClient.connection.getNetworkManager(), sPacketUpdateTileEntity);

//                    LOGGER.error("Received invalid update packet for null tile entity at {} with data: {}", blockPos, nbt);
                    return;
                }
            }
        }
    }
}
