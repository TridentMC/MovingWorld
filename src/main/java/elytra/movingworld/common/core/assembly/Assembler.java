package elytra.movingworld.common.core.assembly;

import elytra.movingworld.MovingWorldMod;
import elytra.movingworld.common.core.util.ITickingTask;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

/**
 * Uses a flood fill algorithm to create a BlockMap, based on an area in world.
 * <p/>
 * Not a mandatory implementation, but it's nice to have.
 */
public class Assembler implements ITickingTask {

    public BlockPos initialOffset;
    boolean foundAll;
    World world;
    BlockCollection out;
    IAssemblyListener assemblyListener;
    private AssemblyInteractor interactor;
    private BlockPos ORIGIN;
    private ArrayList<BlockPos> posStack;

    private int lifeTime = 0;

    public Assembler(AssemblyInteractor interactor, World world, BlockPos startAt, boolean init) {
        this.interactor = interactor;
        this.world = world;
        this.posStack = new ArrayList<BlockPos>();
        this.posStack.add(startAt);
        this.ORIGIN = startAt;

        if (init)
            initAndRegister();
    }

    public void initAndRegister() {
        if (interactor.useInteraction() && !interactor.selfIterate())
            MovingWorldMod.proxy.registerTickable(this);
    }

    public void setAssemblyListener(IAssemblyListener assemblyListener) {
        this.assemblyListener = assemblyListener;
    }

    @Override
    public Side side() {
        return Side.SERVER;
    }

    @Override
    public boolean begin(Side side) {
        out = new BlockCollection(ORIGIN);

        return true;
    }

    @Override
    public void doTick(Side side) {
        if (side.isClient())
            return;
        lifeTime++;

        int currentIteration = 0;

        if (!posStack.isEmpty()) {
            while (!posStack.isEmpty()) {
                if (currentIteration >= iterationsPerTick()) {
                    break;
                }

                BlockPos pos = posStack.get(posStack.size() - 1);
                posStack.remove(posStack.size() - 1);
                IBlockState blockState = world.getBlockState(pos);
                TileEntity tile = world.getTileEntity(pos);

                if (blockState != null && !(blockState.getBlock() instanceof BlockAir)) {
                    if (out.containsBlockAtPosition(pos)) {
                        currentIteration++;
                        continue;
                    }

                    out.addToMap(pos, blockState, tile);

                    posStack.add(pos.add(1, 0, 0));
                    posStack.add(pos.add(0, 1, 0));
                    posStack.add(pos.add(0, 0, 1));
                    posStack.add(pos.add(-1, 0, 0));
                    posStack.add(pos.add(0, -1, 0));
                    posStack.add(pos.add(0, 0, -1));
                }

                currentIteration++;
                continue;
            }
        } else {
            foundAll = true;
        }
    }

    @Override
    public boolean complete(Side side) {
        boolean isDone = out.size() >= interactor.maxSize() || foundAll;

        if (!isDone)
            return false;

        BlockPos shift = new BlockPos(ORIGIN.getX(), 0, ORIGIN.getZ());
        out.toString();

        out.shiftPosition(shift, false);
        initialOffset = new BlockPos(out.getMin().getX(), 0, out.getMin().getZ());
        out.shiftPosition(new BlockPos(out.getMin().getX(), 0, out.getMin().getZ()), false);

        if (MovingWorldMod.instance.inDev())
            MovingWorldMod.logger.info("Flood Filler filled " + out.size() + " in " + lifeTime + " ticks. (About " + Math.round(lifeTime / 20) + " seconds.)");

        assemblyListener.onComplete(world, ORIGIN, out);

        return true;
    }

    public int iterationsPerTick() {
        return interactor.useInteraction() ? interactor.iterationsPerTick() : 128;
    }

    public interface IAssemblyListener {
        void onComplete(World world, BlockPos origin, BlockCollection map);
    }

}
