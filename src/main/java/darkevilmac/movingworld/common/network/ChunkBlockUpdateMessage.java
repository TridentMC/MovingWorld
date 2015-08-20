package darkevilmac.movingworld.common.network;

import cpw.mods.fml.relauncher.Side;
import darkevilmac.movingworld.common.chunk.ChunkIO;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkPosition;

import java.io.IOException;
import java.util.Collection;

public class ChunkBlockUpdateMessage extends EntityMovingWorldMessage {
    private Collection<ChunkPosition> sendQueue;

    public ChunkBlockUpdateMessage() {
    }

    public ChunkBlockUpdateMessage(EntityMovingWorld movingWorld, Collection<ChunkPosition> blocks) {
        super(movingWorld);
        sendQueue = blocks;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.encodeInto(ctx, buf, side);
        try {
            ChunkIO.writeCompressed(buf, movingWorld.getMovingWorldChunk(), sendQueue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player, Side side) {
        super.decodeInto(ctx, buf, player, side);
        if (movingWorld != null) {
            try {
                ChunkIO.readCompressed(buf, movingWorld.getMovingWorldChunk());
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
