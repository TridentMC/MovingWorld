package darkevilmac.movingworld.chunk.assembly;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.LocatedBlock;
import darkevilmac.movingworld.chunk.MovingWorldAssemblyInteractor;
import darkevilmac.movingworld.chunk.MovingWorldSizeOverflowException;
import darkevilmac.movingworld.event.AssembleBlockEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ChunkAssembler {
    public final BlockPos start;
    private final int maxBlocks;
    private World worldObj;

    public ChunkAssembler(World world, BlockPos startPos, int maxMovingWorldBlocks) {
        worldObj = world;
        start = startPos;
        maxBlocks = maxMovingWorldBlocks;
    }

    public AssembleResult doAssemble(MovingWorldAssemblyInteractor interactor) {
        AssembleResult result = new AssembleResult();
        result.offset = start;
        result.assemblyInteractor = interactor;
        try {
            if (MovingWorld.instance.mConfig.iterativeAlgorithm) {
                assembleIterative(result, result.assemblyInteractor, start);
            } else {
                assembleRecursive(result, new HashSet<BlockPos>(), result.assemblyInteractor, start);
            }
            if (result.movingWorldMarkingBlock == null) {
                result.resultCode = AssembleResult.RESULT_MISSING_MARKER;
            } else {
                result.resultCode = AssembleResult.RESULT_OK;
            }
        } catch (MovingWorldSizeOverflowException e) {
            result.resultCode = AssembleResult.RESULT_BLOCK_OVERFLOW;
        } catch (Error e) {
            result.resultCode = AssembleResult.RESULT_ERROR_OCCURED;
        }
        result.assemblyInteractor.chunkAssembled(result);
        return result;
    }

    private void assembleIterative(AssembleResult result, MovingWorldAssemblyInteractor assemblyInteractor, BlockPos sPos) throws MovingWorldSizeOverflowException {
        HashSet<BlockPos> openSet = new HashSet<BlockPos>();
        HashSet<BlockPos> closedSet = new HashSet<BlockPos>();
        List<BlockPos> iterator = new ArrayList<BlockPos>();

        LocatedBlock movingWorldMarker = null;

        BlockPos pos = sPos;

        openSet.add(new BlockPos(sPos));
        while (!openSet.isEmpty()) {
            iterator.addAll(openSet);
            for (BlockPos iPos : iterator) {
                openSet.remove(iPos);

                if (closedSet.contains(iPos)) {
                    continue;
                }
                if (result.assembledBlocks.size() > maxBlocks) {
                    throw new MovingWorldSizeOverflowException();
                }

                pos = new BlockPos(iPos);

                closedSet.add(iPos);

                IBlockState blockState = worldObj.getBlockState(pos);
                Block block = blockState.getBlock();
                if (!canUseBlockForVehicle(block, assemblyInteractor, pos)) {
                    continue;
                }

                LocatedBlock lb = new LocatedBlock(blockState, worldObj.getTileEntity(pos), iPos);
                assemblyInteractor.blockAssembled(lb);
                if (assemblyInteractor.isTileMovingWorldMarker(lb.tileEntity) || assemblyInteractor.isBlockMovingWorldMarker(lb.blockState.getBlock())) {
                    if (movingWorldMarker == null)
                        movingWorldMarker = lb;
                }
                AssembleBlockEvent event = new AssembleBlockEvent(lb);
                MinecraftForge.EVENT_BUS.post(event);
                result.assembleBlock(lb);

                openSet.add(pos.add(-1, 0, 0));
                openSet.add(pos.add(0, -1, 0));
                openSet.add(pos.add(0, 0, -1));
                openSet.add(pos.add(1, 0, 0));
                openSet.add(pos.add(0, 1, 0));
                openSet.add(pos.add(0, 0, 1));

                if (assemblyInteractor.doDiagonalAssembly()) {
                    openSet.add(pos.add(-1, -1, +0));
                    openSet.add(pos.add(+1, -1, +0));
                    openSet.add(pos.add(+1, +1, +0));
                    openSet.add(pos.add(-1, +1, +0));

                    openSet.add(pos.add(-1, +0, -1));
                    openSet.add(pos.add(+1, +0, -1));
                    openSet.add(pos.add(+1, +0, +1));
                    openSet.add(pos.add(-1, +0, +1));

                    openSet.add(pos.add(+0, -1, -1));
                    openSet.add(pos.add(+0, +1, -1));
                    openSet.add(pos.add(+0, +1, +1));
                    openSet.add(pos.add(+0, -1, +1));
                }
            }
        }
        result.movingWorldMarkingBlock = movingWorldMarker;
    }

    private void assembleRecursive(AssembleResult result, HashSet<BlockPos> set, MovingWorldAssemblyInteractor assemblyInteractor, BlockPos pos) throws MovingWorldSizeOverflowException {
        LocatedBlock movingWorldMarker = null;

        if (result.assembledBlocks.size() > maxBlocks) {
            throw new MovingWorldSizeOverflowException();
        }

        if (set.contains(pos)) return;

        set.add(pos);
        IBlockState blockState = worldObj.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!canUseBlockForVehicle(block, assemblyInteractor, pos)) return;

        LocatedBlock lb = new LocatedBlock(blockState, worldObj.getTileEntity(pos), pos);
        assemblyInteractor.blockAssembled(lb);
        if (assemblyInteractor.isBlockMovingWorldMarker(block) || assemblyInteractor.isTileMovingWorldMarker(lb.tileEntity)) {
            if (movingWorldMarker == null)
                movingWorldMarker = lb;
        }
        AssembleBlockEvent event = new AssembleBlockEvent(lb);
        MinecraftForge.EVENT_BUS.post(event);
        result.assembleBlock(lb);

        assembleRecursive(result, set, assemblyInteractor, pos.add(-1, +0, +0));
        assembleRecursive(result, set, assemblyInteractor, pos.add(+0, -1, +0));
        assembleRecursive(result, set, assemblyInteractor, pos.add(+0, +0, -1));
        assembleRecursive(result, set, assemblyInteractor, pos.add(+1, +0, +0));
        assembleRecursive(result, set, assemblyInteractor, pos.add(+0, +1, +0));
        assembleRecursive(result, set, assemblyInteractor, pos.add(+0, +0, +1));

        if (assemblyInteractor.doDiagonalAssembly()) {
            assembleRecursive(result, set, assemblyInteractor, pos.add(-1, -1, +0));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+1, -1, +0));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+1, +1, +0));
            assembleRecursive(result, set, assemblyInteractor, pos.add(-1, +1, +0));

            assembleRecursive(result, set, assemblyInteractor, pos.add(-1, +0, -1));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+1, +0, -1));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+1, +0, +1));
            assembleRecursive(result, set, assemblyInteractor, pos.add(-1, +0, +1));

            assembleRecursive(result, set, assemblyInteractor, pos.add(+0, -1, -1));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+0, +1, -1));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+0, +1, +1));
            assembleRecursive(result, set, assemblyInteractor, pos.add(+0, -1, +1));
        }
        result.movingWorldMarkingBlock = movingWorldMarker;
    }

    public boolean canUseBlockForVehicle(Block block, MovingWorldAssemblyInteractor assemblyInteractor, BlockPos pos) {
        return assemblyInteractor.isBlockAllowed(worldObj, block, pos);
    }
}
