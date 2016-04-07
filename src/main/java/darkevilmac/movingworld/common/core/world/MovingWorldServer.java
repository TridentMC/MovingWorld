package darkevilmac.movingworld.common.core.world;

import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.common.core.IMovingWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.vecmath.Vector3f;
import java.io.File;

public class MovingWorldServer extends WorldServer implements IMovingWorld {

    /**
     * The world we reside in, we forward a lot of boring methods over to it. Like getWorldTime.
     */
    public WorldServer parentWorld;
    public Vec3d worldPosition;
    private Integer id;

    private BlockPos min;
    private BlockPos max;
    private BlockPos coreBlock;

    public MovingWorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
        super(server, saveHandlerIn, info, dimensionId, profilerIn);

        if (info instanceof MovingWorldInfo) {
            ((MovingWorldInfo) info).movingWorld = this;
        }

        min = new BlockPos(0, 0, 0);
        max = new BlockPos(0, 0, 0);
        coreBlock = new BlockPos(0, 0, 0);
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
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (setBlockState(pos, newState, flags)) {
            this.min = new BlockPos(pos.getX() < min.getX() ? pos.getX() : min.getX(),
                    pos.getY() < min.getY() ? pos.getY() : min.getY(),
                    pos.getZ() < min.getZ() ? pos.getZ() : min.getZ());
            this.max = new BlockPos(pos.getX() > max.getX() ? pos.getX() : max.getX(),
                    pos.getY() > max.getY() ? pos.getY() : max.getY(),
                    pos.getZ() > max.getZ() ? pos.getZ() : max.getZ());

            return true;
        }
        return false;
    }

    @Override
    public void doPark() {

    }

    @Override
    public void unPark() {

    }

    @Override
    public boolean isParked() {
        return false;
    }

    @Override
    public BlockPos translateToBlockSpace(Vec3d worldSpace) {
        return null;
    }

    @Override
    public Vec3d translateToWorldSpace(BlockPos blockSpace) {
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
    public Vec3d worldTranslation() {
        return null;
    }

    @Override
    public Vec3d scale() {
        return null;
    }

    @Override
    public Vector3f rotation() {
        return null;
    }

    @Override
    public World parent() {
        return parentWorld;
    }

    @Override
    public Integer id() {
        return id;
    }

    @Override
    public BlockPos coreBlock() {
        return coreBlock;
    }

    @Override
    public IMovingWorld setCoreBlock(BlockPos pos) {
        this.coreBlock = pos;
        return this;
    }

    @Override
    public boolean move(Vec3d move, boolean teleport) {
        if (teleport) {
            // A teleport won't fail ever, it ignores collision.

            worldPosition = move;

            return true;
        }

        return false;
    }

    @Override
    public Vec3d motion() {
        return null;
    }

    @Override
    public IMovingWorld setMotion(Vec3d newMotion) {
        return this;
    }

    @Override
    public IMovingWorld setParent(World world) {
        if (world == null || !(world instanceof WorldServer))
            return this;

        this.parentWorld = (WorldServer) world;
        return this;
    }

    @Override
    public IMovingWorld setId(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean isInRangeToLoad(Vec3d pos) {
        boolean inRange;

        AxisAlignedBB area = area(false);
        area = area.expand(64, 64, 64);
        inRange = area.isVecInside(pos);

        return inRange;
    }

    @Override
    public AxisAlignedBB area(boolean internal) {
        if (internal)
            return new AxisAlignedBB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        else {
            Vec3d adjust = new Vec3d((worldPosition.xCoord + coreBlock.getX()),
                    (worldPosition.yCoord + coreBlock.getX()),
                    (worldPosition.zCoord + coreBlock.getZ()));

            Vec3d min = new Vec3d(adjust.xCoord + min().getX(),
                    adjust.yCoord + min().getY(),
                    adjust.zCoord + min().getZ());
            Vec3d max = new Vec3d(adjust.xCoord + max().getX(),
                    adjust.yCoord + max().getY(),
                    adjust.zCoord + max().getZ());

            return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
        }
    }

    @Override
    public java.io.File getChunkSaveLocation() {
        return new File(new File(parentWorld.getChunkSaveLocation(), "MovingWorld"), id().toString());
    }

    public void onConstruct() {
        this.id = MovingWorldMod.movingWorldFactory.currentID;
        this.parentWorld = (WorldServer) MovingWorldMod.movingWorldFactory.currentParent;
    }
}
