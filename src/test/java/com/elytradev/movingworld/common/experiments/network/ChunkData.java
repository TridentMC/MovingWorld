package com.elytradev.movingworld.common.experiments.network;

import com.elytradev.concrete.Marshallable;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.elytradev.movingworld.common.experiments.BlockData;
import com.elytradev.movingworld.common.experiments.MobileRegion;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Serializable compressed chunk data to send to clients.
 * <p>
 * Based off of the old ChunkIO class because it still works and I like it.
 */
public class ChunkData implements Marshallable {

    private World world;
    private MobileRegion region;
    private List<BlockData> blockDatas = Lists.newArrayList();

    public ChunkData() {
    }

    public ChunkData(World world, MobileRegion region) {
        this.world = world;
        this.region = region;
    }

    public List<BlockData> getBlockData() {
        return blockDatas;
    }

    private void writeBlock(DataOutput out, MobileChunk chunk, BlockPos pos) throws IOException {
        writeBlock(out, chunk.getBlockState(pos), pos);
    }

    private void writeBlock(DataOutput out, IBlockState state, BlockPos pos) throws IOException {
        out.writeInt(pos.getX());
        out.writeInt(pos.getY());
        out.writeInt(pos.getZ());
        out.writeShort(Block.getIdFromBlock(state.getBlock()));
        out.writeInt(state.getBlock().getMetaFromState(state));
    }

    public void writeAllCompressed(ByteBuf dataOut) throws IOException {
        DataOutputStream out = preCompress(dataOut);
        int count = writeAll(out);
        postCompress(dataOut, out, count);
    }

    private int writeAll(DataOutput dataOut) throws IOException {
        int countedBlocks = 0;

        for (BlockPos pos : BlockPos.getAllInBox(region.minBlockPos(), region.maxBlockPos())) {
            IBlockState stateAtPos = world.getBlockState(pos);
            if (stateAtPos != null && !Objects.equals(stateAtPos.getBlock(), Blocks.AIR)) {
                countedBlocks++;
            }
        }

        dataOut.writeInt(countedBlocks);
        for (BlockPos pos : BlockPos.getAllInBox(region.minBlockPos(), region.maxBlockPos())) {
            IBlockState stateAtPos = world.getBlockState(pos);
            if (stateAtPos != null && !Objects.equals(stateAtPos.getBlock(), Blocks.AIR)) {
                writeBlock(dataOut, stateAtPos, pos);
            }
        }

        return countedBlocks;
    }

    private DataOutputStream preCompress(ByteBuf dataOut) throws IOException {
        ByteBufOutputStream bbos = new ByteBufOutputStream(dataOut);
        DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bbos));
        return out;
    }

    private void postCompress(ByteBuf dataOut, DataOutputStream out, int count) throws IOException {
        out.flush();
        out.close();

        int byteswritten = dataOut.writerIndex();
        float f = (float) byteswritten / (count * 9);
        //MovingWorldMod.LOG.debug(String.format(Locale.ENGLISH, "%d blocks written. Efficiency: %d/%d = %.2f", count, byteswritten, count * 9, f));

        if (byteswritten > 32000) {
            //MovingWorldMod.LOG.warn("MobileRegion *probably* contains too many blocks");
        }
    }

    public void readCompressed(ByteBuf data) throws IOException {
        DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteBufInputStream(data)));
        read(in);
        in.close();
    }

    @SuppressWarnings("deprecation")
    private void read(DataInput in) throws IOException {
        int count = in.readInt();

        //MovingWorldMod.LOG.debug("Reading mobile chunk data: " + count + " blocks");

        int x, y, z;
        int id;
        IBlockState state;
        for (int i = 0; i < count; i++) {
            x = in.readInt();
            y = in.readInt();
            z = in.readInt();
            id = in.readShort();
            state = Block.getBlockById(id).getStateFromMeta(in.readInt());
            blockDatas.add(new BlockData(new BlockPos(x, y, z), state));
        }
    }

    @Override
    public void writeToNetwork(ByteBuf buf) {
        try {
            writeAllCompressed(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readFromNetwork(ByteBuf buf) {
        try {
            readCompressed(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
