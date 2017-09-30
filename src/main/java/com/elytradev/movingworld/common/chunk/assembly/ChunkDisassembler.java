package com.elytradev.movingworld.common.chunk.assembly;

import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.api.IMovingTile;
import com.elytradev.movingworld.common.chunk.LocatedBlock;
import com.elytradev.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.event.DisassembleBlockEvent;
import com.elytradev.movingworld.common.tile.TileMovingMarkingBlock;
import com.elytradev.movingworld.common.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

public class ChunkDisassembler {
    public boolean overwrite;
    private EntityMovingWorld movingWorld;

    private AssembleResult result;
    private LocatedBlockList removedFluidBlocks;
    private TileMovingMarkingBlock tileMarker;

    public ChunkDisassembler(EntityMovingWorld EntityMovingWorld) {
        movingWorld = EntityMovingWorld;
        overwrite = false;
    }

    public boolean canDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        if (overwrite) {
            return true;
        }
        World world = movingWorld.world;
        MobileChunk chunk = movingWorld.getMobileChunk();
        float yaw = Math.round(movingWorld.rotationYaw / 90F) * 90F;
        yaw = (float) Math.toRadians(yaw);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a VehicleFiller, this value will always be 0.
        float oz = -chunk.getCenterZ();

        Vec3dMod vec;
        IBlockState state;
        Block block;
        BlockPos pos;
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    if (chunk.isAirBlock(new BlockPos(i, j, k))) continue;

                    vec = new Vec3dMod(i + ox, j + oy, k + oz);
                    vec = vec.rotateAroundY(yaw);

                    pos = new BlockPos(MathHelperMod.round_double(vec.x + movingWorld.posX),
                            MathHelperMod.round_double(vec.y + movingWorld.posY),
                            MathHelperMod.round_double(vec.z + movingWorld.posZ));

                    state = world.getBlockState(pos);
                    block = state.getBlock();
                    if ((block != null && !block.isAir(state, world, pos) && !block.getMaterial(state).isLiquid() && !assemblyInteractor.canOverwriteState(state))
                            || (MathHelperMod.round_double(vec.y + movingWorld.posY) > world.getActualHeight())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public AssembleResult doDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        movingWorld.disassembling = true;
        tileMarker = null;
        if (movingWorld.getMobileChunk().marker != null && movingWorld.getMobileChunk().marker.tileEntity != null && movingWorld.getMobileChunk().marker.tileEntity instanceof TileMovingMarkingBlock)
            tileMarker = (TileMovingMarkingBlock) movingWorld.getMobileChunk().marker.tileEntity;

        removedFluidBlocks = new LocatedBlockList();
        World world = movingWorld.getEntityWorld();
        MobileChunk chunk = movingWorld.getMobileChunk();
        LocatedBlockList fillableBlocks = new FloodFiller().floodFillMobileChunk(chunk);
        this.result = new AssembleResult();
        result.offset = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        int currentRot = Math.round(movingWorld.rotationYaw / 90F);
        movingWorld.rotationYaw = currentRot * 90F;
        movingWorld.rotationPitch = 0F;
        float yaw = currentRot * MathHelperMod.PI_HALF;

        boolean flag = world.getGameRules().getBoolean("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        LocatedBlockList postList = new LocatedBlockList(4);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a ChunkAssembler, this value will always be 0.
        float oz = -chunk.getCenterZ();

        LocatedBlockList lbList = new LocatedBlockList();

        Vec3dMod vec;
        TileEntity tileentity;
        IBlockState blockState;
        BlockPos pos;
        try {
            for (int i = chunk.minX(); i < chunk.maxX(); i++) {
                for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                    for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                        blockState = chunk.getBlockState(new BlockPos(i, j, k));
                        if (blockState.getBlock() == Blocks.AIR) {
                            if (blockState.getBlock().getMetaFromState(blockState) == 1) continue;
                        } else if (blockState.getBlock().isAir(blockState, world, new BlockPos(i, j, k)))
                            continue;
                        tileentity = chunk.getTileEntity(new BlockPos(i, j, k));

                        vec = new Vec3dMod(i + ox, j + oy, k + oz);
                        vec = vec.rotateAroundY(yaw);

                        pos = new BlockPos(MathHelperMod.round_double(vec.x + movingWorld.posX),
                                MathHelperMod.round_double(vec.y + movingWorld.posY),
                                MathHelperMod.round_double(vec.z + movingWorld.posZ));

                        lbList.add(new LocatedBlock(blockState, tileentity, pos, new BlockPos(i, j, k)));
                    }
                }
            }

            ArrayList<LocatedBlockList> separatedLbLists = lbList.getSortedDisassemblyBlocks();

            for (LocatedBlockList locatedBlockList : separatedLbLists) {
                if (locatedBlockList != null && !locatedBlockList.isEmpty()) {
                    postList = processLocatedBlockList(world, locatedBlockList, postList, assemblyInteractor, fillableBlocks, currentRot);
                }
            }
        } catch (Exception exception) {
            world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));
            MovingWorldMod.LOG.error("Exception while disassembling, reverting doTileDrops... ", exception);
            this.result.resultType = AssembleResult.ResultType.RESULT_ERROR_OCCURED;
            return this.result;
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));

        ArrayList<LocatedBlockList> sortedPostList = postList.getSortedDisassemblyBlocks();

        for (LocatedBlockList pList : sortedPostList) {
            if (pList != null && !pList.isEmpty())
                for (LocatedBlock locatedBlockInstance : pList) {
                    pos = locatedBlockInstance.blockPos;
                    MovingWorldMod.LOG.debug("Post-rejoining block: " + locatedBlockInstance.toString());
                    world.setBlockState(pos, locatedBlockInstance.blockState, 2);
                    assemblyInteractor.blockDisassembled(locatedBlockInstance);
                    DisassembleBlockEvent event = new DisassembleBlockEvent(movingWorld, locatedBlockInstance);
                    MinecraftForge.EVENT_BUS.post(event);
                    this.result.assembleBlock(locatedBlockInstance);
                }
        }

        if (tileMarker != null) {
            tileMarker.removedFluidBlocks = removedFluidBlocks;
        }

        movingWorld.setDead();

        if (this.result.movingWorldMarkingBlock == null || !assemblyInteractor.isTileMovingWorldMarker(this.result.movingWorldMarkingBlock.tileEntity)) {
            this.result.resultType = AssembleResult.ResultType.RESULT_MISSING_MARKER;
        } else {
            result.checkConsistent(world);
        }
        assemblyInteractor.chunkDissasembled(this.result);
        this.result.assemblyInteractor = assemblyInteractor;

        return result;
    }

    LocatedBlockList processLocatedBlockList(World world, LocatedBlockList locatedBlocks, LocatedBlockList postList, MovingWorldAssemblyInteractor assemblyInteractor, LocatedBlockList fillList, int currentRot) {
        LocatedBlockList retPostList = new LocatedBlockList();
        retPostList.addAll(postList);

        TileEntity tileentity;
        IBlockState blockState;
        BlockPos pos;
        IBlockState owBlockState;
        Block owBlock;

        for (LocatedBlock locatedBlock : locatedBlocks) {
            locatedBlock = rotateBlock(locatedBlock, currentRot);

            int i = locatedBlock.bPosNoOffset.getX();
            int j = locatedBlock.bPosNoOffset.getY();
            int k = locatedBlock.bPosNoOffset.getZ();

            pos = locatedBlock.blockPos;
            blockState = locatedBlock.blockState;
            tileentity = locatedBlock.tileEntity;
            blockState = assemblyInteractor.blockRotated(blockState, currentRot);

            owBlockState = world.getBlockState(pos);
            owBlock = owBlockState.getBlock();
            if (owBlock != null)
                assemblyInteractor.blockOverwritten(owBlock);

            if (!fillList.containsLBOfPos(locatedBlock.bPosNoOffset)) {
                if (world.getBlockState(pos).getMaterial().isLiquid()) {
                    if (!removedFluidBlocks.containsLBOfPos(pos))
                        removedFluidBlocks.add(new LocatedBlock(owBlockState, pos));
                }
                if (!world.setBlockState(pos, blockState, 2) || blockState.getBlock() != world.getBlockState(pos).getBlock()) {
                    retPostList.add(new LocatedBlock(blockState, tileentity, pos));
                    continue;
                }
                if (blockState != world.getBlockState(pos)) {
                    world.setBlockState(pos, blockState, 2);
                }
            }
            if (tileentity != null) {
                tileentity.setPos(pos);
                if (tileentity instanceof IMovingTile) {
                    ((IMovingTile) tileentity).setParentMovingWorld(null, new BlockPos(i, j, k));
                }
                NBTTagCompound tileTag = new NBTTagCompound();
                tileentity.writeToNBT(tileTag);
                world.setTileEntity(pos, tileentity);
                world.getTileEntity(pos).readFromNBT(tileTag);
                tileentity.validate();
                tileentity = world.getTileEntity(pos);

                if (tileMarker != null && tileMarker.getPos().equals(tileentity.getPos())) {
                    tileMarker = (TileMovingMarkingBlock) tileentity;
                }
            }

            blockState = world.getBlockState(pos);
            tileentity = world.getTileEntity(pos);

            LocatedBlock lb = new LocatedBlock(blockState, tileentity, pos);
            assemblyInteractor.blockDisassembled(lb);
            DisassembleBlockEvent event = new DisassembleBlockEvent(movingWorld, lb);
            MinecraftForge.EVENT_BUS.post(event);
            result.assembleBlock(lb);
        }

        return retPostList;
    }

    private LocatedBlock rotateBlock(LocatedBlock locatedBlock, int deltaRot) {
        deltaRot &= 3;
        if (deltaRot != 0) {
            for (int r = 0; r < deltaRot; r++) {
                locatedBlock = RotationHelper.rotateBlock(locatedBlock, true);
            }
        }
        return locatedBlock;
    }
}
