package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.MovingWorld;
import net.minecraft.block.Block;
import net.minecraft.world.ChunkPosition;
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

    public AssembleResult doAssemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        AssembleResult result = new AssembleResult();
        result.xOffset = startX;
        result.yOffset = startY;
        result.zOffset = startZ;
        try {
            if (MovingWorld.instance.mConfig.iterativeAlgorithm) {
                assembleIterative(result, assemblyInteractor, startX, startY, startZ);
            } else {
                assembleRecursive(result, new HashSet<ChunkPosition>(), assemblyInteractor, startX, startY, startZ);
            }
            if (result.movingWorldMarkingBlock == null) {
                result.resultCode = AssembleResult.RESULT_MISSING_MARKER;
            } else {
                result.resultCode = AssembleResult.RESULT_OK;
            }
        } catch (MovingWorldSizeOverflowException e) {
            result.resultCode = AssembleResult.RESULT_BLOCK_OVERFLOW;
        } catch (Error e) {
            MovingWorld.logger.error("Error while assembling Moving World instance.", e);
            result.resultCode = AssembleResult.RESULT_ERROR_OCCURED;
        }
        result.assemblyInteractor = assemblyInteractor;
        assemblyInteractor.chunkAssembled(result);
        return result;
    }

    private void assembleIterative(AssembleResult result, MovingWorldAssemblyInteractor assemblyInteractor, int sx, int sy, int sz) throws MovingWorldSizeOverflowException {
        HashSet<ChunkPosition> openSet = new HashSet<ChunkPosition>();
        HashSet<ChunkPosition> closedSet = new HashSet<ChunkPosition>();
        List<ChunkPosition> iterator = new ArrayList<ChunkPosition>();

        int x = sx, y = sy, z = sz;

        openSet.add(new ChunkPosition(sx, sy, sz));
        while (!openSet.isEmpty()) {
            iterator.addAll(openSet);
            for (ChunkPosition pos : iterator) {
                openSet.remove(pos);

                if (closedSet.contains(pos)) {
                    continue;
                }
                if (result.assembledBlocks.size() > maxBlocks) {
                    throw new MovingWorldSizeOverflowException();
                }

                x = pos.chunkPosX;
                y = pos.chunkPosY;
                z = pos.chunkPosZ;

                closedSet.add(pos);

                Block block = worldObj.getBlock(x, y, z);
                if (!canUseBlockForVehicle(block, assemblyInteractor, x, y, z)) {
                    continue;
                }

                LocatedBlock lb = new LocatedBlock(block, worldObj.getBlockMetadata(x, y, z), worldObj.getTileEntity(x, y, z), pos);
                assemblyInteractor.blockAssembled(lb);
                result.assembleBlock(lb);

                openSet.add(new ChunkPosition(x - 1, y, z));
                openSet.add(new ChunkPosition(x, y - 1, z));
                openSet.add(new ChunkPosition(x, y, z - 1));
                openSet.add(new ChunkPosition(x + 1, y, z));
                openSet.add(new ChunkPosition(x, y + 1, z));
                openSet.add(new ChunkPosition(x, y, z + 1));

                if (assemblyInteractor.doDiagonalAssembly()) {
                    openSet.add(new ChunkPosition(x - 1, y - 1, z));
                    openSet.add(new ChunkPosition(x + 1, y - 1, z));
                    openSet.add(new ChunkPosition(x + 1, y + 1, z));
                    openSet.add(new ChunkPosition(x - 1, y + 1, z));

                    openSet.add(new ChunkPosition(x - 1, y, z - 1));
                    openSet.add(new ChunkPosition(x + 1, y, z - 1));
                    openSet.add(new ChunkPosition(x + 1, y, z + 1));
                    openSet.add(new ChunkPosition(x - 1, y, z + 1));

                    openSet.add(new ChunkPosition(x, y - 1, z - 1));
                    openSet.add(new ChunkPosition(x, y + 1, z - 1));
                    openSet.add(new ChunkPosition(x, y + 1, z + 1));
                    openSet.add(new ChunkPosition(x, y - 1, z + 1));
                }
            }
        }
    }

    private void assembleRecursive(AssembleResult result, HashSet<ChunkPosition> set, MovingWorldAssemblyInteractor assemblyInteractor, int x, int y, int z) throws MovingWorldSizeOverflowException {
        if (result.assembledBlocks.size() > maxBlocks) {
            throw new MovingWorldSizeOverflowException();
        }

        ChunkPosition pos = new ChunkPosition(x, y, z);
        if (set.contains(pos)) return;

        set.add(pos);
        Block block = worldObj.getBlock(x, y, z);
        if (!canUseBlockForVehicle(block, assemblyInteractor, x, y, z)) return;

        LocatedBlock lb = new LocatedBlock(block, worldObj.getBlockMetadata(x, y, z), worldObj.getTileEntity(x, y, z), pos);
        assemblyInteractor.blockAssembled(lb);
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
    }

    public boolean canUseBlockForVehicle(Block block, MovingWorldAssemblyInteractor assemblyInteractor, int x, int y, int z) {
        return assemblyInteractor.isBlockAllowed(worldObj, block, x, y, z);
    }
}
