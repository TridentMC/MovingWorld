package elytra.movingworld.common.core.world;

import elytra.movingworld.MovingWorldMod;
import elytra.movingworld.common.core.IMovingWorld;
import elytra.movingworld.common.util.Vec3dMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.lwjgl.util.vector.Vector2f;

import java.io.File;

public class MovingWorldServer extends WorldServer implements IMovingWorld {

    /**
     * The world we reside in, we forward a lot of boring methods over to it. Like getWorldTime.
     */
    public WorldServer parentWorld;
    private Integer id;

    private BlockPos min = new BlockPos(0, 0, 0);
    private BlockPos max = new BlockPos(0, 0, 0);
    private BlockPos coreBlock = new BlockPos(0, 0, 0);

    public Vec3d prevPos = Vec3d.ZERO;
    public Vec3d pos = Vec3d.ZERO;
    public Vec3d motion = Vec3d.ZERO;
    public Vector2f rotation = new Vector2f(0, 0);
    public Vector2f prevRotation = new Vector2f(0, 0);

    public MovingWorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
        super(server, saveHandlerIn, info, dimensionId, profilerIn);

        if (info != null && info instanceof MovingWorldInfo) {
            ((MovingWorldInfo) info).movingWorld = this;
        }
    }

    public void setWorldInfo(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
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
    public EnumDifficulty getDifficulty() {
        return this.parent().getDifficulty();
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (super.setBlockState(pos, newState, flags)) {
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
        Vec3dMod vec = new Vec3dMod(blockSpace.getX(), blockSpace.getY(), blockSpace.getZ());
        float yaw = Math.round(rotation.getX());
        yaw = (float) Math.toRadians(yaw);
        vec = vec.rotateAroundY(yaw);

        Vec3d worldSpace = new Vec3d((vec.xCoord + pos.xCoord),
                (vec.yCoord + pos.yCoord),
                (vec.zCoord + pos.zCoord));

        return worldSpace;
        //TODO: Could be broken, I don't know it's loosely based off of the old AssembleResult canAssemble code.
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public Vec3d worldTranslation() {
        return pos;
    }

    @Override
    public Vec3d scale() {
        return new Vec3d(1, 1, 1);
    }

    @Override
    public Vector2f rotation() {
        return rotation;
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
            if (pos != null)
                prevPos = pos;
            else
                prevPos = Vec3d.ZERO;
            pos = move;

            return true;
        }

        return false;
    }

    @Override
    public Vec3d motion() {
        return motion;
    }

    @Override
    public IMovingWorld setMotion(Vec3d newMotion) {
        this.motion = newMotion;

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
    public IMovingWorld setRotation(Vector2f rotation) {
        if (this.rotation != null)
            prevRotation = this.rotation;
        else
            prevRotation = new Vector2f(0, 0);

        this.rotation = rotation;
        return this;
    }

    @Override
    public IMovingWorld setBounds(BlockPos min, BlockPos max) {
        this.min = min;
        this.max = max;

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
            Vec3d adjust = new Vec3d((pos.xCoord + coreBlock.getX()),
                    (pos.yCoord + coreBlock.getX()),
                    (pos.zCoord + coreBlock.getZ()));

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
    public File getChunkSaveLocation() {
        return new File(new File(parentWorld.getChunkSaveLocation(), "MovingWorld"), id().toString());
    }

    public void onConstruct() {
        this.id = MovingWorldMod.movingWorldFactory.currentID;
        this.parentWorld = (WorldServer) MovingWorldMod.movingWorldFactory.currentParent;
    }
}
