package darkevilmac.movingworld.common.network;

import com.unascribed.lambdanetwork.*;
import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.chunk.ChunkIO;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import darkevilmac.movingworld.common.tile.TileMovingWorldMarkingBlock;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class MovingWorldNetworking {

    public static LambdaNetwork NETWORK;

    public static void setupNetwork() {
        MovingWorld.logger.info("Setting up network...");
        MovingWorldNetworking.NETWORK = registerPackets(LambdaNetwork.builder().channel("MovingWorld")).build();
        MovingWorld.logger.info("Setup network! " + MovingWorldNetworking.NETWORK.toString());
    }

    private static LambdaNetworkBuilder registerPackets(LambdaNetworkBuilder builder) {
        builder = builder.packet("FarInteractMessage").boundTo(Side.SERVER)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID").handledBy(new BiConsumer<EntityPlayer, Token>() {
                    @Override
                    public void accept(EntityPlayer entityPlayer, Token token) {
                        World world = DimensionManager.getWorld(token.getInt("dimID"));
                        if (world != null) {
                            Entity unCast = world.getEntityByID(token.getInt("entityID"));

                            if (unCast != null && unCast instanceof EntityMovingWorld) {
                                EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;

                                entityPlayer.interactWith(movingWorld);
                            }
                        }
                    }
                });

        builder = builder.packet("ChunkBlockUpdateMessage").boundTo(Side.CLIENT)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.ARBITRARY, "chunk").handledOnMainThreadBy(new BiConsumer<EntityPlayer, Token>() {
                    @Override
                    public void accept(EntityPlayer entityPlayer, Token token) {
                        ByteBuf buf = Unpooled.wrappedBuffer(token.getData("chunk"));

                        World world = DimensionManager.getWorld(token.getInt("dimID"));
                        if (world != null) {
                            Entity unCast = world.getEntityByID(token.getInt("entityID"));

                            if (unCast != null && unCast instanceof EntityMovingWorld) {
                                EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;
                                try {
                                    ChunkIO.readCompressed(buf, movingWorld.getMobileChunk());
                                } catch (IOException e) {
                                    MovingWorld.logger.error(e);
                                }
                            }
                        }
                    }
                });

        builder = builder.packet("TileEntitiesMessage").boundTo(Side.CLIENT)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.NBT_COMPOUND, "tagCompound").handledOnMainThreadBy(new BiConsumer<EntityPlayer, Token>() {
                    @Override
                    public void accept(EntityPlayer entityPlayer, Token token) {
                        NBTTagCompound tagCompound = token.getNBT("tagCompound");
                        World world = DimensionManager.getWorld(token.getInt("dimID"));
                        if (world != null) {
                            Entity unCast = world.getEntityByID(token.getInt("entityID"));

                            if (unCast != null && unCast instanceof EntityMovingWorld) {
                                EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;

                                if (movingWorld != null && tagCompound != null && movingWorld.getMobileChunk() != null &&
                                        movingWorld.getMobileChunk() instanceof MobileChunkClient) {
                                    NBTTagList list = tagCompound.getTagList("list", 10);
                                    for (int i = 0; i < list.tagCount(); i++) {
                                        NBTTagCompound nbt = list.getCompoundTagAt(i);
                                        if (nbt == null) continue;
                                        int x = nbt.getInteger("x");
                                        int y = nbt.getInteger("y");
                                        int z = nbt.getInteger("z");
                                        BlockPos pos = new BlockPos(x, y, z);
                                        try {
                                            TileEntity te = movingWorld.getMobileChunk().getTileEntity(pos);
                                            if (te != null)
                                                te.readFromNBT(nbt);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    ((MobileChunkClient) movingWorld.getMobileChunk()).getRenderer().markDirty();
                                }
                            }
                        }
                    }
                });

        builder = builder.packet("RequestMovingWorldDataMessage").boundTo(Side.SERVER)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .handledOnMainThreadBy(new BiConsumer<EntityPlayer, Token>() {
                    @Override
                    public void accept(EntityPlayer entityPlayer, Token token) {
                        World world = DimensionManager.getWorld(token.getInt("dimID"));
                        if (world != null) {
                            Entity unCast = world.getEntityByID(token.getInt("entityID"));

                            if (unCast != null && unCast instanceof EntityMovingWorld) {
                                EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;

                                NBTTagCompound tagCompound = new NBTTagCompound();
                                NBTTagList list = new NBTTagList();
                                for (TileEntity te : movingWorld.getMobileChunk().chunkTileEntityMap.values()) {
                                    NBTTagCompound nbt = new NBTTagCompound();
                                    if (te instanceof TileMovingWorldMarkingBlock) {
                                        ((TileMovingWorldMarkingBlock) te).writeNBTForSending(nbt);
                                    } else {
                                        te.writeToNBT(nbt);
                                    }
                                    list.appendTag(nbt);
                                }
                                tagCompound.setTag("list", list);

                                NETWORK.send().packet("TileEntitiesMessage")
                                        .with("dimID", token.getInt("dimID"))
                                        .with("entityID", token.getInt("entityID"))
                                        .with("tagCompound", tagCompound)
                                        .to(entityPlayer);
                            }
                        }
                    }
                });

        //builder = builder.packet("ConfigMessage").boundTo(Side.CLIENT)
        //        .with(DataType.STRING, "config")
        //        .with(DataType.BOOLEAN, "restore")
        //        .handledBy(new BiConsumer<EntityPlayer, Token>() {
        //            @Override
        //            public void accept(EntityPlayer entityPlayer, Token token) {
        //                MovingWorldConfig.SharedConfig config = null;
        //                if (!token.getBoolean("restore")) {
        //                    config = new Gson().fromJson(token.getString("config"), MovingWorldConfig.SharedConfig.class);
        //                }
//
        //                if (MovingWorld.proxy != null && MovingWorld.proxy instanceof ClientProxy) {
        //                    if (config != null) {
        //                        ((ClientProxy) MovingWorld.proxy).syncedConfig = MovingWorld.instance.getLocalConfig();
        //                        ((ClientProxy) MovingWorld.proxy).syncedConfig.setShared(config);
        //                    } else {
        //                        ((ClientProxy) MovingWorld.proxy).syncedConfig = null;
        //                    }
        //                }
//
        //            }
        //        });

        builder = builder.packet("MovingWorldClientActionMessage").boundTo(Side.SERVER)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.BYTE, "action").handledOnMainThreadBy(new BiConsumer<EntityPlayer, Token>() {
                    @Override
                    public void accept(EntityPlayer entityPlayer, Token token) {
                        MovingWorldClientAction action = MovingWorldClientAction.fromByte((byte) token.getInt("action"));
                        World world = DimensionManager.getWorld(token.getInt("dimID"));
                        if (world != null) {
                            Entity unCast = world.getEntityByID(token.getInt("entityID"));

                            if (unCast != null && unCast instanceof EntityMovingWorld) {
                                EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;

                                if (movingWorld != null && movingWorld.riddenByEntity == entityPlayer) {
                                    switch (action) {
                                        case DISASSEMBLE:
                                            movingWorld.alignToGrid();
                                            movingWorld.updateRiderPosition(entityPlayer, movingWorld.riderDestination, 1);
                                            movingWorld.disassemble(false);
                                            break;
                                        case DISASSEMBLEOVERWRITE:
                                            movingWorld.alignToGrid();
                                            movingWorld.updateRiderPosition(entityPlayer, movingWorld.riderDestination, 1);
                                            movingWorld.disassemble(true);
                                            break;
                                        case ALIGN:
                                            movingWorld.alignToGrid();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }

                    }
                });

        return builder;
    }

}
