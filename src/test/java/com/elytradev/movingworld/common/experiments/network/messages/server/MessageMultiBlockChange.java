package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Marshallable;
import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.marshallers.BlockStateMarshaller;
import com.elytradev.movingworld.common.experiments.network.marshallers.BlockUpdateDataMarshaller;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

/**
 * Created by darkevilmac on 2/25/2017.
 */
@ReceivedOn(Side.CLIENT)
public class MessageMultiBlockChange extends Message {

    @MarshalledAs("varint")
    public int dimension;

    @MarshalledAs("varint")
    public int chunkX;
    @MarshalledAs("varint")
    public int chunkZ;
    @MarshalledAs(BlockUpdateDataMarshaller.MARSHALLER_NAME + "-list")
    public List<BlockUpdateData> changedBlocks;

    public MessageMultiBlockChange(NetworkContext ctx) {
        super(ctx);
    }

    public MessageMultiBlockChange(int changeCount, short[] offsets, Chunk chunk) {
        super(MovingWorldExperimentsNetworking.networkContext);

        this.chunkX = chunk.xPosition;
        this.chunkZ = chunk.zPosition;
        this.changedBlocks = Lists.newArrayListWithCapacity(changeCount);

        for (int i = 0; i < this.changedBlocks.size(); ++i) {
            changedBlocks.add(i, new BlockUpdateData(offsets[i], chunk));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void handle(EntityPlayer sender) {
        WorldClient worldClient = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(dimension);

        for (BlockUpdateData data : changedBlocks) {
            worldClient.invalidateRegionAndSetBlock(data.getPos(chunkX, chunkZ), data.getBlockState());
        }
    }

    public static class BlockUpdateData implements Marshallable {
        private short offset;
        private IBlockState blockState;

        public BlockUpdateData(short offset, IBlockState state) {
            this.offset = offset;
            this.blockState = state;
        }

        public BlockUpdateData(short offset, Chunk chunk) {
            this.offset = offset;
            this.blockState = chunk.getBlockState(this.getPos(chunk.getPos().chunkXPos, chunk.getPos().chunkZPos));
        }

        public BlockUpdateData() {
        }

        public BlockPos getPos(int chunkX, int chunkZ) {
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

            return new BlockPos(chunkPos.getBlock(this.offset >> 12 & 15, this.offset & 255, this.offset >> 8 & 15));
        }

        public short getOffset() {
            return this.offset;
        }

        public IBlockState getBlockState() {
            return this.blockState;
        }

        @Override
        public void writeToNetwork(ByteBuf buf) {
            buf.writeShort(offset);
            BlockStateMarshaller.INSTANCE.marshal(buf, blockState);
        }

        @Override
        public void readFromNetwork(ByteBuf buf) {
            this.offset = buf.readShort();
            blockState = BlockStateMarshaller.INSTANCE.unmarshal(buf);
        }
    }

}
