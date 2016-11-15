package io.github.elytra.movingworld.common.network;

import com.unascribed.lambdanetwork.DataType;
import com.unascribed.lambdanetwork.LambdaNetwork;
import com.unascribed.lambdanetwork.LambdaNetworkBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.chunk.ChunkIO;
import io.github.elytra.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;
import io.github.elytra.movingworld.common.tile.TileMovingMarkingBlock;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MovingWorldNetworking {

    public static LambdaNetwork NETWORK;

    public static void setupNetwork() {
        //Init net code with builder.

        MovingWorldMod.LOG.info("Setting up network...");
        MovingWorldNetworking.NETWORK = registerPackets(LambdaNetwork.builder().channel("MovingWorld")).build();
        MovingWorldMod.LOG.info("Setup network! " + MovingWorldNetworking.NETWORK.toString());
    }

    private static LambdaNetworkBuilder registerPackets(LambdaNetworkBuilder builder) {
        builder = builder.packet("FarInteractMessage").boundTo(Side.SERVER)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.INT, "hand")
                .with(DataType.NBT_COMPOUND, "stack")
                .handledBy((entityPlayer, token) -> {
                    World world = DimensionManager.getWorld(token.getInt("dimID"));
                    if (world != null) {
                        Entity unCast = world.getEntityByID(token.getInt("entityID"));

                        if (unCast != null && unCast instanceof EntityMovingWorld) {
                            EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;
                            EnumHand hand = EnumHand.values()[token.getInt("hand")];
                            ItemStack stack = null;
                            if (token.getNBT("stack") != null)
                                stack = ItemStack.loadItemStackFromNBT(token.getNBT("stack"));

                            entityPlayer.interact(movingWorld, stack, hand);
                        }
                    }
                });

        builder = builder.packet("ChunkBlockUpdateMessage").boundTo(Side.CLIENT)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.ARBITRARY, "chunk").handledOnMainThreadBy((entityPlayer, token) -> {
                    ByteBuf buf = Unpooled.wrappedBuffer(token.getData("chunk"));
                    World world = DimensionManager.getWorld(token.getInt("dimID"));
                    if (world != null) {
                        Entity unCast = world.getEntityByID(token.getInt("entityID"));

                        if (unCast != null && unCast instanceof EntityMovingWorld) {
                            EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;
                            try {
                                ChunkIO.readCompressed(buf, movingWorld.getMobileChunk());
                            } catch (IOException e) {
                                MovingWorldMod.LOG.error(e);
                            }
                        }
                    }
                });

        builder = builder.packet("TileEntitiesMessage").boundTo(Side.CLIENT)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.NBT_COMPOUND, "tagCompound").handledOnMainThreadBy((entityPlayer, token) -> {
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
                });

        builder = builder.packet("RequestMovingWorldDataMessage").boundTo(Side.SERVER)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .handledOnMainThreadBy((entityPlayer, token) -> {
                    World world = DimensionManager.getWorld(token.getInt("dimID"));
                    if (world != null) {
                        Entity unCast = world.getEntityByID(token.getInt("entityID"));

                        if (unCast != null && unCast instanceof EntityMovingWorld) {
                            EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;

                            NBTTagCompound tagCompound = new NBTTagCompound();
                            NBTTagList list = new NBTTagList();
                            for (TileEntity te : movingWorld.getMobileChunk().chunkTileEntityMap.values()) {
                                NBTTagCompound nbt = new NBTTagCompound();
                                if (te instanceof TileMovingMarkingBlock) {
                                    ((TileMovingMarkingBlock) te).writeNBTForSending(nbt);
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
                });

        builder = builder.packet("MovingWorldClientActionMessage").boundTo(Side.SERVER)
                .with(DataType.INT, "dimID")
                .with(DataType.INT, "entityID")
                .with(DataType.BYTE, "action").handledOnMainThreadBy((entityPlayer, token) -> {
                    MovingWorldClientAction action = MovingWorldClientAction.fromByte((byte) token.getInt("action"));
                    World world = DimensionManager.getWorld(token.getInt("dimID"));
                    if (world != null) {
                        Entity unCast = world.getEntityByID(token.getInt("entityID"));

                        if (unCast != null && unCast instanceof EntityMovingWorld) {
                            EntityMovingWorld movingWorld = (EntityMovingWorld) unCast;

                            if (movingWorld != null && movingWorld.getControllingPassenger().getEntityId() == entityPlayer.getEntityId()) {
                                switch (action) {
                                    case DISASSEMBLE:
                                        movingWorld.alignToGrid(true);
                                        movingWorld.updatePassengerPosition(entityPlayer, movingWorld.riderDestination, 1);
                                        movingWorld.removePassengers();
                                        movingWorld.disassemble(false);
                                        break;
                                    case DISASSEMBLEWITHOVERWRITE:
                                        movingWorld.alignToGrid(true);
                                        movingWorld.updatePassengerPosition(entityPlayer, movingWorld.riderDestination, 1);
                                        movingWorld.removePassengers();
                                        movingWorld.disassemble(true);
                                        break;
                                    case ALIGN:
                                        movingWorld.alignToGrid(true);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }

                });

        return builder;
    }

}
