package darkevilmac.movingworld.common.core;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.util.UUID;

public class MovingWorldServer extends WorldServer implements IMovingWorld {

    /**
     * The world we reside in, we forward a lot of boring methods over to it. Like getWorldTime.
     */
    public WorldServer parentWorld;

    public Vec3 worldPosition;

    private UUID id;


    public MovingWorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, UUID id) {
        super(server, saveHandlerIn, info, dimensionId, profilerIn);
    }

    @Override
    public long getTotalWorldTime() {
        return parentWorld.getTotalWorldTime();
    }

    @Override
    public long getWorldTime() {
        return parentWorld.getWorldTime();
    }


    @Override
    public BlockPos translateToBlockSpace(Vec3 worldSpace) {
        return null;
    }

    @Override
    public Vec3 translateToWorldSpace(BlockPos blockSpace) {
        return null;
    }

    @Override
    public BlockPos min() {
        return null;
    }

    @Override
    public BlockPos max() {
        return null;
    }

    @Override
    public Vec3 worldTranslation() {
        return null;
    }

    @Override
    public Vec3 scale() {
        return null;
    }

    @Override
    public Vec3 rotation() {
        return null;
    }

    @Override
    public World parent() {
        return null;
    }

    @Override
    public UUID identifier() {
        return null;
    }

    @Override
    public boolean move(Vec3 move, boolean teleport) {
        return false;
    }
}
