package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.marshallers.ClientEntityMarshaller;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * SPacketChunkData, modified for use with Concrete and subworlds.
 */
@ReceivedOn(Side.CLIENT)
public class MessageChunkData extends Message {

    @MarshalledAs(ClientEntityMarshaller.MARSHALLER_NAME)
    private EntityMobileRegion mobileRegion;

    @MarshalledAs("u16")
    private int dimension;

    @MarshalledAs("i8")
    private int chunkX;
    @MarshalledAs("i8")
    private int chunkZ;
    @MarshalledAs("i8")
    private int availableSections;
    private boolean loadChunk;

    private ByteBuf buffer;
    @MarshalledAs("nbt-list")
    private List<NBTTagCompound> tileEntityTags;

    public MessageChunkData(NetworkContext ctx) {
        super(ctx);
    }

    public MessageChunkData(EntityMobileRegion mobileRegion, Chunk chunkIn, int changedSectionFilter) {
        super(MovingWorldExperimentsNetworking.networkContext);

        this.mobileRegion = mobileRegion;
        this.dimension = chunkIn.getWorld().provider.getDimension();
        this.chunkX = chunkIn.xPosition;
        this.chunkZ = chunkIn.zPosition;
        this.loadChunk = changedSectionFilter == 65535;
        boolean flag = chunkIn.getWorld().provider.hasSkyLight();
        this.buffer = Unpooled.wrappedBuffer(new byte[this.calculateChunkSize(chunkIn, flag, changedSectionFilter)]);
        buffer.writerIndex(0);
        this.availableSections = this.extractChunkData(new PacketBuffer(buffer), chunkIn, flag, changedSectionFilter);
        this.tileEntityTags = Lists.newArrayList();

        for (Map.Entry<BlockPos, TileEntity> entry : chunkIn.getTileEntityMap().entrySet()) {
            BlockPos blockpos = entry.getKey();
            TileEntity tileentity = entry.getValue();
            int i = blockpos.getY() >> 4;

            if (this.doChunkLoad() || (changedSectionFilter & 1 << i) != 0) {
                NBTTagCompound nbttagcompound = tileentity.getUpdateTag();
                this.tileEntityTags.add(nbttagcompound);
            }
        }
    }

    public PacketBuffer getReadBuffer() {
        return new PacketBuffer(Unpooled.wrappedBuffer(this.buffer));
    }

    @Override
    protected void handle(EntityPlayer sender) {
        mobileRegion.setupClientForData();

        WorldClient worldClient = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(dimension);

        if (loadChunk) {
            worldClient.doPreChunk(chunkX, chunkZ, true);
        }

        worldClient.invalidateBlockReceiveRegion(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 256, (chunkZ << 4) + 15);
        Chunk chunk = worldClient.getChunkFromChunkCoords(chunkX, chunkZ);
        chunk.fillChunk(getReadBuffer(), availableSections, loadChunk);
        worldClient.markBlockRangeForRenderUpdate(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 256, (chunkZ << 4) + 15);

        if (!loadChunk || !(worldClient.provider instanceof WorldProviderSurface)) {
            chunk.resetRelightChecks();
        }

        for (NBTTagCompound nbttagcompound : tileEntityTags) {
            BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
            TileEntity tileentity = worldClient.getTileEntity(blockpos);

            if (tileentity != null) {
                tileentity.handleUpdateTag(nbttagcompound);
            }
        }

        for (int x = chunk.getPos().getXStart(); x < chunk.getPos().getXEnd(); x++) {
            for (int z = chunk.getPos().getZStart(); z < chunk.getPos().getZEnd(); z++) {
                for (int y = 0; y < chunk.getWorld().getHeight(); y++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    if (state == null || Objects.equals(state.getBlock(), Blocks.AIR))
                        continue;

                    System.out.println(state);
                }
            }
        }
    }

    public int extractChunkData(PacketBuffer buf, Chunk chunkIn, boolean writeSkylight, int changedSectionFilter) {
        int i = 0;
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        int j = 0;

        for (int k = aextendedblockstorage.length; j < k; ++j) {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];

            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!this.doChunkLoad() || !extendedblockstorage.isEmpty()) && (changedSectionFilter & 1 << j) != 0) {
                i |= 1 << j;
                extendedblockstorage.getData().write(buf);
                buf.writeBytes(extendedblockstorage.getBlocklightArray().getData());

                if (writeSkylight) {
                    buf.writeBytes(extendedblockstorage.getSkylightArray().getData());
                }
            }
        }

        if (this.doChunkLoad()) {
            buf.writeBytes(chunkIn.getBiomeArray());
        }

        return i;
    }

    protected int calculateChunkSize(Chunk chunkIn, boolean p_189556_2_, int p_189556_3_) {
        int i = 0;
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        int j = 0;

        for (int k = aextendedblockstorage.length; j < k; ++j) {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];

            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!this.doChunkLoad() || !extendedblockstorage.isEmpty()) && (p_189556_3_ & 1 << j) != 0) {
                i = i + extendedblockstorage.getData().getSerializedSize();
                i = i + extendedblockstorage.getBlocklightArray().getData().length;

                if (p_189556_2_) {
                    i += extendedblockstorage.getSkylightArray().getData().length;
                }
            }
        }

        if (this.doChunkLoad()) {
            i += chunkIn.getBiomeArray().length;
        }

        return i;
    }

    public boolean doChunkLoad() {
        return this.loadChunk;
    }

}
