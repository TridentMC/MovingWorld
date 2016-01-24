package darkevilmac.movingworld.common.core.assembly;

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

    boolean foundAll;
    World world;
    BlockMap out;
    private ArrayList<BlockPos> posStack;

    public Assembler(AssemblyInteractor interactor, World world, BlockPos startAt) {
        this.interactor = interactor;
        this.world = world;
        this.posStack = new ArrayList<BlockPos>();
        this.posStack.add(startAt);
    }

    public void initAndRegister() {
        //TODO: Tick handler.
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
                return;
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
    }

    @Override
    public boolean complete(Side side) {
        return out.size() >= interactor.maxSize() || foundAll;
    }

    @Override
    public int iterationsPerTick() {
        return interactor.useInteraction() ? interactor.iterationsPerTick() : 30;
    }

    public abstract class AssemblyInteractor {
        /**
         * Should the flood fill go diagonal, or just to it's direct NSWE neighbors?
         * <p/>
         * If you're confused please see here: https://en.wikipedia.org/wiki/Flood_fill
         * <p/>
         * 4 directions is most similar to false, 8 directions is most similar to true.
         */
        public abstract boolean doDiagonal();

        /**
         * Should we even bother listening to this interactor or is it a stub?
         */
        public abstract boolean useInteraction();

        /**
         * @return How many times can we try to find new blocks on a single minecraft tick?
         */
        public abstract int iterationsPerTick();

        /**
         * Should the assembler register itself to be ticked automatically or are you going to call tick every tick?
         */
        public abstract boolean selfIterate();

        /**
         * Once we reach this many blocks found, or we're unable to find any more we stop iterating and set complete to true.
         */
        public abstract int maxSize();
    }
}
