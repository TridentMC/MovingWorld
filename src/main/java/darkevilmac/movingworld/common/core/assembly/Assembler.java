package darkevilmac.movingworld.common.core.assembly;

import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.common.core.util.ITickBasedIterable;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

/**
 * Uses a flood fill algorithm to create a BlockMap, based on an area in world.
 * <p/>
 * Not a mandatory implementation, but it's nice to have.
 */
public class Assembler implements ITickBasedIterable {

    private AssemblyInteractor interactor;
    private BlockPos ORIGIN;

    boolean foundAll;
    World world;
    BlockMap out;
    IAssemblyListener assemblyListener;
    private ArrayList<BlockPos> posStack;

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
        out = new BlockMap();

        return true;
    }

    @Override
    public void doTick(Side side) {
        int currentIteration = 0;

        while (!posStack.isEmpty()) {
            if (currentIteration >= iterationsPerTick()) {
                break;
            }

            BlockPos pos = posStack.get(posStack.size() - 1);
            posStack.remove(posStack.size() - 1);
            IBlockState blockState = world.getBlockState(pos);
            TileEntity tile = world.getTileEntity(pos);

            if (blockState == null || blockState.getBlock() instanceof BlockAir) {
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

        if (posStack.isEmpty()) {
            foundAll = true;
        }
    }

    @Override
    public boolean complete(Side side) {
        boolean isDone = out.size() >= interactor.maxSize() || foundAll;
        if (isDone)
            assemblyListener.onComplete(world, ORIGIN, out);
        return isDone;
    }

    @Override
    public int iterationsPerTick() {
        return interactor.useInteraction() ? interactor.iterationsPerTick() : 30;
    }

    public interface IAssemblyListener {
        void onComplete(World world, BlockPos origin, BlockMap map);
    }

}
