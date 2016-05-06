package darkevilmac.movingworld.common.network.sync.request;

import darkevilmac.movingworld.asm.mixin.world.MixinWorldServer;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.network.MovingWorldMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A client sends this when a chunk loads on their end.
 */
public class ClientChunkEventMessage extends MovingWorldMessage {

    public int dimension;
    public int chunkX;
    public int chunkZ;

    @Override
    public boolean onMainThread() {
        return false;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        buf.writeInt(dimension);
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        this.dimension = buf.readInt();
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (player != null && DimensionManager.getWorld(dimension) != null) {
            World world = DimensionManager.getWorld(dimension);
            MixinWorldServer worldServerMixed = (MixinWorldServer) (Object) world;

            if (!(world instanceof IMovingWorld) && !world.isRemote) {
                if (!worldServerMixed.getMovingWorlds().isEmpty()) {
                    BlockPos chunkShifted = new BlockPos(
                            chunkX << 4, 0,
                            chunkZ << 4
                    );
                    for (IMovingWorld movingWorld : worldServerMixed.getMovingWorlds()) {
                        if (movingWorld.isInRangeToLoad(new Vec3d(chunkShifted.getX(),
                                movingWorld.worldTranslation().yCoord, chunkShifted.getZ()))) {
                            //TODO: Dispatch packet to initialize a MovingWorldClient.
                        }
                    }
                }
            }
        }
    }
}
