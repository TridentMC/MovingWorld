package darkevilmac.movingworld.common.network;

import darkevilmac.movingworld.common.chunk.ChunkIO;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.Collection;

public class ChunkBlockUpdateMessage extends EntityMovingWorldMessage {
    private Collection<BlockPos> sendQueue;

    public ChunkBlockUpdateMessage() {
    }

    public ChunkBlockUpdateMessage(EntityMovingWorld movingWorld, Collection<BlockPos> blocks) {
        super(movingWorld);
        sendQueue = blocks;
    }

    @Override
    public boolean onMainThread() {
        return false;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.encodeInto(ctx, buf, side);
        try {
            ChunkIO.writeCompressed(buf, movingWorld.getMobileChunk(), sendQueue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.decodeInto(ctx, buf, side);
        if (movingWorld != null) {
            try {
                ChunkIO.readCompressed(buf, movingWorld.getMobileChunk());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        //No implementation required.
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        //No implementation required.
    }
}
