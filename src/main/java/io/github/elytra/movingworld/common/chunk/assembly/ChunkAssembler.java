package io.github.elytra.movingworld.common.chunk.assembly;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.chunk.LocatedBlock;
import io.github.elytra.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import io.github.elytra.movingworld.common.chunk.MovingWorldSizeOverflowException;
import io.github.elytra.movingworld.common.event.AssembleBlockEvent;


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
            if (MovingWorldMod.INSTANCE.getNetworkConfig().getShared().iterativeAlgorithm) {
                assembleIterative(result, result.assemblyInteractor, start);
            } else {
                assembleRecursive(result, new HashSet<>(), result.assemblyInteractor, start);
            }
            if (result.movingWorldMarkingBlock == null) {
                result.resultType = AssembleResult.ResultType.RESULT_MISSING_MARKER;
            } else {
                result.resultType = AssembleResult.ResultType.RESULT_OK;
            }
        } catch (MovingWorldSizeOverflowException e) {
            result.resultType = AssembleResult.ResultType.RESULT_BLOCK_OVERFLOW;
        } catch (Error e) {
            result.resultType = AssembleResult.ResultType.RESULT_ERROR_OCCURED;
            MovingWorldMod.LOG.error(e.toString());
        }
        result.assemblyInteractor.chunkAssembled(result);
        return result;
    }

    private void assembleIterative(AssembleResult result, MovingWorldAssemblyInteractor assemblyInteractor, BlockPos worldPos) throws MovingWorldSizeOverflowException {
        HashSet<BlockPos> openSet = new HashSet<>();
        HashSet<BlockPos> closedSet = new HashSet<>();
        List<BlockPos> iterator = new ArrayList<>();

        LocatedBlock movingWorldMarker = null;

        openSet.add(new BlockPos(worldPos));
        while (!openSet.isEmpty()) {
            iterator.addAll(openSet);
            for (BlockPos mobileChunkPos : iterator) {
                openSet.remove(mobileChunkPos);

                if (closedSet.contains(mobileChunkPos)) {
                    continue;
                }
                if (result.assembledBlocks.size() > maxBlocks) {
                    throw new MovingWorldSizeOverflowException();
                }

                worldPos = new BlockPos(mobileChunkPos);

                closedSet.add(mobileChunkPos);

                IBlockState blockState = worldObj.getBlockState(worldPos);
                CanAssemble canAssemble = canUseBlockForVehicle(new LocatedBlock(blockState, worldObj.getTileEntity(worldPos), worldPos), assemblyInteractor);

                if (canAssemble.justCancel) {
                    continue;
                }

                LocatedBlock lb = new LocatedBlock(blockState, worldObj.getTileEntity(worldPos), mobileChunkPos);
                assemblyInteractor.blockAssembled(lb);
                if (assemblyInteractor.isTileMovingWorldMarker(lb.tileEntity) || assemblyInteractor.isBlockMovingWorldMarker(lb.blockState.getBlock())) {
                    if (movingWorldMarker == null)
                        movingWorldMarker = lb;
                }
                AssembleBlockEvent event = new AssembleBlockEvent(lb);
                MinecraftForge.EVENT_BUS.post(event);
                result.assembleBlock(lb);

                if (!canAssemble.assembleThenCancel) {
                    openSet.add(worldPos.add(-1, 0, 0));
                    openSet.add(worldPos.add(0, -1, 0));
                    openSet.add(worldPos.add(0, 0, -1));
                    openSet.add(worldPos.add(1, 0, 0));
                    openSet.add(worldPos.add(0, 1, 0));
                    openSet.add(worldPos.add(0, 0, 1));

                    if (assemblyInteractor.doDiagonalAssembly()) {
                        openSet.add(worldPos.add(-1, -1, +0));
                        openSet.add(worldPos.add(+1, -1, +0));
                        openSet.add(worldPos.add(+1, +1, +0));
                        openSet.add(worldPos.add(-1, +1, +0));

                        openSet.add(worldPos.add(-1, +0, -1));
                        openSet.add(worldPos.add(+1, +0, -1));
                        openSet.add(worldPos.add(+1, +0, +1));
                        openSet.add(worldPos.add(-1, +0, +1));

                        openSet.add(worldPos.add(+0, -1, -1));
                        openSet.add(worldPos.add(+0, +1, -1));
                        openSet.add(worldPos.add(+0, +1, +1));
                        openSet.add(worldPos.add(+0, -1, +1));
                    }
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

        CanAssemble canAssemble = canUseBlockForVehicle(new LocatedBlock(blockState, worldObj.getTileEntity(pos), pos), assemblyInteractor);

        if (canAssemble.justCancel) {
            return;
        }

        LocatedBlock lb = new LocatedBlock(blockState, worldObj.getTileEntity(pos), pos);
        assemblyInteractor.blockAssembled(lb);
        if (assemblyInteractor.isBlockMovingWorldMarker(block) || assemblyInteractor.isTileMovingWorldMarker(lb.tileEntity)) {
            if (movingWorldMarker == null)
                movingWorldMarker = lb;
        }
        AssembleBlockEvent event = new AssembleBlockEvent(lb);
        MinecraftForge.EVENT_BUS.post(event);
        result.assembleBlock(lb);

        if (!canAssemble.assembleThenCancel) {
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
        }
        result.movingWorldMarkingBlock = movingWorldMarker;
    }

    public CanAssemble canUseBlockForVehicle(LocatedBlock lb, MovingWorldAssemblyInteractor assemblyInteractor) {
        return assemblyInteractor.isBlockAllowed(worldObj, lb);
    }
}
