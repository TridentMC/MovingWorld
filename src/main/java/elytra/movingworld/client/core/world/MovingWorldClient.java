package elytra.movingworld.client.core.world;

import elytra.movingworld.common.core.IMovingWorld;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import org.lwjgl.util.vector.Vector2f;


public class MovingWorldClient extends WorldClient implements IMovingWorld {

    public MovingWorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(netHandler, settings, dimension, difficulty, profilerIn);
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
    public Vector2f rotation() {
        return null;
    }

    @Override
    public World parent() {
        return null;
    }

    @Override
    public Integer id() {
        return null;
    }

    @Override
    public BlockPos coreBlock() {
        return null;
    }

    @Override
    public IMovingWorld setCoreBlock(BlockPos pos) {
        return null;
    }

    @Override
    public boolean move(Vec3d move, boolean teleport) {
        return false;
    }

    @Override
    public Vec3d motion() {
        return null;
    }

    @Override
    public IMovingWorld setMotion(Vec3d newMotion) {
        return null;
    }

    @Override
    public IMovingWorld setParent(World world) {
        return null;
    }

    @Override
    public IMovingWorld setId(Integer id) {
        return null;
    }

    @Override
    public IMovingWorld setRotation(Vector2f rotation) {
        return null;
    }

    @Override
    public IMovingWorld setBounds(BlockPos min, BlockPos max) {
        return null;
    }

    @Override
    public boolean isInRangeToLoad(Vec3d pos) {
        return false;
    }

    @Override
    public AxisAlignedBB area(boolean internal) {
        return null;
    }
}
