package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.block.BlockMovingWorldMarker;
import darkevilmac.movingworld.tile.TileMovingWorldMarkingBlock;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ChunkAssembler {
    public final int startX, startY, startZ;
    private final int maxBlocks;
    private World worldObj;


    public ChunkAssembler(World world, int x, int y, int z, int maxMovingWorldBlocks) {
        worldObj = world;

        startX = x;
        startY = y;
        startZ = z;

        maxBlocks = maxMovingWorldBlocks;
    }

    public AssembleResult doAssemble(MovingWorldAssemblyInteractor interactor) {
        AssembleResult result = new AssembleResult();
        result.xOffset = startX;
        result.yOffset = startY;
        result.zOffset = startZ;
        result.assemblyInteractor = interactor;
        try {
            if (MovingWorld.instance.mConfig.iterativeAlgorithm) {
                assembleIterative(result, result.assemblyInteractor, startX, startY, startZ);
            } else {
                assembleRecursive(result, new HashSet<BlockPos>(), result.assemblyInteractor, startX, startY, startZ);
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

    private void assembleIterative(AssembleResult result, MovingWorldAssemblyInteractor assemblyInteractor, int sx, int sy, int sz) throws MovingWorldSizeOverflowException {
        HashSet<BlockPos> openSet = new HashSet<BlockPos>();
        HashSet<BlockPos> closedSet = new HashSet<BlockPos>();
        List<BlockPos> iterator = new ArrayList<BlockPos>();

        LocatedBlock movingWorldMarker = null;

        int x = sx, y = sy, z = sz;

        openSet.add(new BlockPos(sx, sy, sz));
        while (!openSet.isEmpty()) {
            iterator.addAll(openSet);
            for (BlockPos pos : iterator) {
                openSet.remove(pos);

                if (closedSet.contains(pos)) {
                    continue;
                }
                if (result.assembledBlocks.size() > maxBlocks) {
                    throw new MovingWorldSizeOverflowException();
                }

                x = pos.getX();
                y = pos.getY();
                z = pos.getZ();

                closedSet.add(pos);

                Block block = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (!canUseBlockForVehicle(block, assemblyInteractor, x, y, z)) {
                    continue;
                }

                LocatedBlock lb = new LocatedBlock(block, block.getMetaFromState(worldObj.getBlockState(new BlockPos(x, y, z))), worldObj.getTileEntity(new BlockPos(x, y, z)), pos);
                assemblyInteractor.blockAssembled(lb);
                if ((lb.block != null && lb.block instanceof BlockMovingWorldMarker) || (lb.tileEntity != null && lb.tileEntity instanceof TileMovingWorldMarkingBlock)) {
                    if (movingWorldMarker == null)
                        movingWorldMarker = lb;
                }
                result.assembleBlock(lb);

                openSet.add(new BlockPos(x - 1, y, z));
                openSet.add(new BlockPos(x, y - 1, z));
                openSet.add(new BlockPos(x, y, z - 1));
                openSet.add(new BlockPos(x + 1, y, z));
                openSet.add(new BlockPos(x, y + 1, z));
                openSet.add(new BlockPos(x, y, z + 1));

                if (assemblyInteractor.doDiagonalAssembly()) {
                    openSet.add(new BlockPos(x - 1, y - 1, z));
                    openSet.add(new BlockPos(x + 1, y - 1, z));
                    openSet.add(new BlockPos(x + 1, y + 1, z));
                    openSet.add(new BlockPos(x - 1, y + 1, z));

                    openSet.add(new BlockPos(x - 1, y, z - 1));
                    openSet.add(new BlockPos(x + 1, y, z - 1));
                    openSet.add(new BlockPos(x + 1, y, z + 1));
                    openSet.add(new BlockPos(x - 1, y, z + 1));

                    openSet.add(new BlockPos(x, y - 1, z - 1));
                    openSet.add(new BlockPos(x, y + 1, z - 1));
                    openSet.add(new BlockPos(x, y + 1, z + 1));
                    openSet.add(new BlockPos(x, y - 1, z + 1));
                }
            }
        }
        result.movingWorldMarkingBlock = movingWorldMarker;
    }

    private void assembleRecursive(AssembleResult result, HashSet<BlockPos> set, MovingWorldAssemblyInteractor assemblyInteractor, int x, int y, int z) throws MovingWorldSizeOverflowException {
        LocatedBlock movingWorldMarker = null;

        if (result.assembledBlocks.size() > maxBlocks) {
            throw new MovingWorldSizeOverflowException();
        }

        BlockPos pos = new BlockPos(x, y, z);
        if (set.contains(pos)) return;

        set.add(pos);
        Block block = worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
        if (!canUseBlockForVehicle(block, assemblyInteractor, x, y, z)) return;

        LocatedBlock lb = new LocatedBlock(block, block.getMetaFromState(worldObj.getBlockState(new BlockPos(x, y, z))), worldObj.getTileEntity(new BlockPos(x, y, z)), pos);
        assemblyInteractor.blockAssembled(lb);
        if ((lb.block != null && lb.block instanceof BlockMovingWorldMarker) || (lb.tileEntity != null && lb.tileEntity instanceof TileMovingWorldMarkingBlock)) {
            if (movingWorldMarker == null)
                movingWorldMarker = lb;
        }
        result.assembleBlock(lb);

        assembleRecursive(result, set, assemblyInteractor, x - 1, y, z);
        assembleRecursive(result, set, assemblyInteractor, x, y - 1, z);
        assembleRecursive(result, set, assemblyInteractor, x, y, z - 1);
        assembleRecursive(result, set, assemblyInteractor, x + 1, y, z);
        assembleRecursive(result, set, assemblyInteractor, x, y + 1, z);
        assembleRecursive(result, set, assemblyInteractor, x, y, z + 1);

        if (assemblyInteractor.doDiagonalAssembly()) {
            assembleRecursive(result, set, assemblyInteractor, x - 1, y - 1, z);
            assembleRecursive(result, set, assemblyInteractor, x + 1, y - 1, z);
            assembleRecursive(result, set, assemblyInteractor, x + 1, y + 1, z);
            assembleRecursive(result, set, assemblyInteractor, x - 1, y + 1, z);

            assembleRecursive(result, set, assemblyInteractor, x - 1, y, z - 1);
            assembleRecursive(result, set, assemblyInteractor, x + 1, y, z - 1);
            assembleRecursive(result, set, assemblyInteractor, x + 1, y, z + 1);
            assembleRecursive(result, set, assemblyInteractor, x - 1, y, z + 1);

            assembleRecursive(result, set, assemblyInteractor, x, y - 1, z - 1);
            assembleRecursive(result, set, assemblyInteractor, x, y + 1, z - 1);
            assembleRecursive(result, set, assemblyInteractor, x, y + 1, z + 1);
            assembleRecursive(result, set, assemblyInteractor, x, y - 1, z + 1);
        }
        result.movingWorldMarkingBlock = movingWorldMarker;
    }

    public boolean canUseBlockForVehicle(Block block, MovingWorldAssemblyInteractor assemblyInteractor, int x, int y, int z) {
        return assemblyInteractor.isBlockAllowed(worldObj, block, x, y, z);
    }
}
