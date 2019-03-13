package com.elytradev.movingworld.common.chunk;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunkServer;
import com.elytradev.movingworld.common.util.LocatedBlockList;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedChunkData {

    private final MobileChunkServer chunk;

    private LocatedBlockList locatedBlocks = new LocatedBlockList();
    private DataOutputStream stream;
    private ByteBuf out;

    public CompressedChunkData(MobileChunkServer mobileChunk, boolean sendAll) {
        this.chunk = mobileChunk;
        try {
            if (sendAll) {
                this.writeAllCompressed();
            } else {
                this.writeCompressed();
            }
        } catch (IOException e) {
            MovingWorldMod.LOG.error("Failed to write mobile chunk data to send over the network. {}", e);
        }
    }

    public CompressedChunkData(ByteBuf from) {
        this.chunk = null;

        try {
            this.readCompressed(from.readBytes(from.readInt()));
        } catch (IOException e) {
            MovingWorldMod.LOG.error("Failed to load mobile chunk data from compressed data. {}", e);
        }
    }

    public void writeTo(ByteBuf buf) {
        buf.writeInt(buf.readableBytes());
        buf.writeBytes(this.out);
    }

    public void loadBlocks(MobileChunk chunk) {
        locatedBlocks.forEach(chunk::setBlockState);
    }

    private void read(DataInput from) throws IOException {
        int count = from.readShort();

        MovingWorldMod.LOG.debug("Reading mobile chunk data: " + count + " blocks");

        int x, y, z;
        IBlockState state;
        for (int i = 0; i < count; i++) {
            x = from.readByte();
            y = from.readByte();
            z = from.readByte();
            state = Block.getStateById(from.readInt());
            this.locatedBlocks.add(new LocatedBlock(state, new BlockPos(x, y, z)));
        }
    }

    private void readCompressed(ByteBuf from) throws IOException {
        DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteBufInputStream(from)));
        read(in);
        in.close();
    }

    private int write() throws IOException {
        Collection<BlockPos> blocks = chunk.getBlockQueue();
        stream.writeShort(blocks.size());
        for (BlockPos p : blocks) {
            writeBlock(chunk.getBlockState(p), p);
        }
        return blocks.size();
    }

    private int writeAll() throws IOException {
        List<BlockPos> nonEmptyPositions = Lists.newArrayList();
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    IBlockState state = chunk.getBlockState(new BlockPos(i, j, k));
                    if (state != null && state.getBlock() != Blocks.AIR) {
                        nonEmptyPositions.add(new BlockPos(i, j, k));
                    }
                }
            }
        }
        MovingWorldMod.LOG.debug("Writing mobile chunk data: " + nonEmptyPositions.size() + " blocks");

        stream.writeShort(nonEmptyPositions.size());
        for (BlockPos pos : nonEmptyPositions) {
            writeBlock(chunk.getBlockState(pos), pos);
        }

        return nonEmptyPositions.size();
    }

    private void writeBlock(IBlockState state, BlockPos pos) throws IOException {
        stream.writeByte(pos.getX());
        stream.writeByte(pos.getY());
        stream.writeByte(pos.getZ());
        stream.writeInt(Block.getStateId(state));
    }

    private void writeCompressed() throws IOException {
        this.stream = this.createStream();
        int size = write();
        this.postCompress(this.out, size);
    }

    private void writeAllCompressed() throws IOException {
        this.stream = this.createStream();
        int size = writeAll();
        this.postCompress(this.out, size);
    }

    private DataOutputStream createStream() throws IOException {
        this.out = Unpooled.buffer();
        ByteBufOutputStream byteBufStream = new ByteBufOutputStream(this.out);
        return new DataOutputStream(new GZIPOutputStream(byteBufStream));
    }

    private void postCompress(ByteBuf data, int size) throws IOException {
        stream.flush();
        stream.close();

        int byteswritten = data.writerIndex();
        float f = (float) byteswritten / (size * 9);
        MovingWorldMod.LOG.debug(String.format(Locale.ENGLISH, "%d blocks written. Efficiency: %d/%d = %.2f", size, byteswritten, size * 9, f));

        if (byteswritten > 32000) {
            MovingWorldMod.LOG.warn("MobileChunk probably contains too many blocks");
        }
    }


}
