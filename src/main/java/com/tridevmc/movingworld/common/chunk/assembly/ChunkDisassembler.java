package com.tridevmc.movingworld.common.chunk.assembly;

import com.tridevmc.movingworld.MovingWorldMod;
import com.tridevmc.movingworld.api.IMovingTile;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.mobilechunk.MobileChunk;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.event.DisassembleBlockEvent;
import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import com.tridevmc.movingworld.common.util.*;
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
        this.movingWorld = EntityMovingWorld;
        this.overwrite = false;
    }

    public boolean canDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        if (this.overwrite) {
            return true;
        }
        World world = this.movingWorld.world;
        MobileChunk chunk = this.movingWorld.getMobileChunk();
        float yaw = Math.round(this.movingWorld.rotationYaw / 90F) * 90F;
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

                    pos = new BlockPos(MathHelperMod.round_double(vec.x + this.movingWorld.posX),
                            MathHelperMod.round_double(vec.y + this.movingWorld.posY),
                            MathHelperMod.round_double(vec.z + this.movingWorld.posZ));

                    state = world.getBlockState(pos);
                    block = state.getBlock();
                    if ((block != null && !block.isAir(state, world, pos) && !block.getMaterial(state).isLiquid() && !assemblyInteractor.canOverwriteState(state))
                            || (MathHelperMod.round_double(vec.y + this.movingWorld.posY) > world.getActualHeight())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public AssembleResult doDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        this.movingWorld.disassembling = true;
        this.tileMarker = null;
        if (this.movingWorld.getMobileChunk().marker != null
                && this.movingWorld.getMobileChunk().marker.tile instanceof TileMovingMarkingBlock)
            this.tileMarker = (TileMovingMarkingBlock) this.movingWorld.getMobileChunk().marker.tile;

        this.removedFluidBlocks = new LocatedBlockList();
        World world = this.movingWorld.getEntityWorld();
        MobileChunk chunk = this.movingWorld.getMobileChunk();
        LocatedBlockList fillableBlocks = new FloodFiller().floodFillMobileChunk(chunk);
        this.result = new AssembleResult();
        this.result.offset = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        int currentRot = Math.round(this.movingWorld.rotationYaw / 90F);
        this.movingWorld.rotationYaw = currentRot * 90F;
        this.movingWorld.rotationPitch = 0F;
        float yaw = currentRot * MathHelperMod.PI_HALF;

        boolean flag = world.getGameRules().getBoolean("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false", world.getServer());

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
                            continue;
                        } else if (blockState.getBlock().isAir(blockState, world, new BlockPos(i, j, k)))
                            continue;
                        tileentity = chunk.getTileEntity(new BlockPos(i, j, k));

                        vec = new Vec3dMod(i + ox, j + oy, k + oz);
                        vec = vec.rotateAroundY(yaw);

                        pos = new BlockPos(MathHelperMod.round_double(vec.x + this.movingWorld.posX),
                                MathHelperMod.round_double(vec.y + this.movingWorld.posY),
                                MathHelperMod.round_double(vec.z + this.movingWorld.posZ));

                        lbList.add(new LocatedBlock(blockState, tileentity, pos, new BlockPos(i, j, k)));
                    }
                }
            }

            ArrayList<LocatedBlockList> separatedLbLists = lbList.getSortedDisassemblyBlocks();

            for (LocatedBlockList locatedBlockList : separatedLbLists) {
                if (locatedBlockList != null && !locatedBlockList.isEmpty()) {
                    postList = this.processLocatedBlockList(world, locatedBlockList, postList, assemblyInteractor, fillableBlocks, currentRot);
                }
            }
        } catch (Exception exception) {
            world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag), world.getServer());
            MovingWorldMod.LOG.error("Exception while disassembling, reverting doTileDrops... ", exception);
            this.result.resultType = AssembleResult.ResultType.RESULT_ERROR_OCCURED;
            return this.result;
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag), world.getServer());

        ArrayList<LocatedBlockList> sortedPostList = postList.getSortedDisassemblyBlocks();

        for (LocatedBlockList pList : sortedPostList) {
            if (pList != null && !pList.isEmpty())
                for (LocatedBlock locatedBlockInstance : pList) {
                    pos = locatedBlockInstance.pos;
                    MovingWorldMod.LOG.debug("Post-rejoining block: " + locatedBlockInstance.toString());
                    world.setBlockState(pos, locatedBlockInstance.state, 2);
                    assemblyInteractor.blockDisassembled(locatedBlockInstance);
                    DisassembleBlockEvent event = new DisassembleBlockEvent(this.movingWorld, locatedBlockInstance);
                    MinecraftForge.EVENT_BUS.post(event);
                    this.result.assembleBlock(locatedBlockInstance);
                }
        }

        if (this.tileMarker != null) {
            this.tileMarker.removedFluidBlocks = this.removedFluidBlocks;
        }

        this.movingWorld.remove();

        if (this.result.movingWorldMarkingBlock == null || !assemblyInteractor.isTileMovingWorldMarker(this.result.movingWorldMarkingBlock.tile)) {
            this.result.resultType = AssembleResult.ResultType.RESULT_MISSING_MARKER;
        } else {
            this.result.checkConsistent(world);
        }
        assemblyInteractor.chunkDissasembled(this.result);
        this.result.assemblyInteractor = assemblyInteractor;

        return this.result;
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
            locatedBlock = this.rotateBlock(locatedBlock, currentRot);

            int i = locatedBlock.posNoOffset.getX();
            int j = locatedBlock.posNoOffset.getY();
            int k = locatedBlock.posNoOffset.getZ();

            pos = locatedBlock.pos;
            blockState = locatedBlock.state;
            tileentity = locatedBlock.tile;
            blockState = assemblyInteractor.blockRotated(blockState, currentRot);

            owBlockState = world.getBlockState(pos);
            owBlock = owBlockState.getBlock();
            if (owBlock != null)
                assemblyInteractor.blockOverwritten(owBlock);

            if (!fillList.containsLBOfPos(locatedBlock.posNoOffset)) {
                if (world.getBlockState(pos).getMaterial().isLiquid()) {
                    if (!this.removedFluidBlocks.containsLBOfPos(pos))
                        this.removedFluidBlocks.add(new LocatedBlock(owBlockState, pos));
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
                tileentity.write(tileTag);
                world.setTileEntity(pos, tileentity);
                world.getTileEntity(pos).read(tileTag);
                tileentity.validate();
                tileentity = world.getTileEntity(pos);

                if (this.tileMarker != null && this.tileMarker.getPos().equals(tileentity.getPos())) {
                    this.tileMarker = (TileMovingMarkingBlock) tileentity;
                }
            }

            blockState = world.getBlockState(pos);
            tileentity = world.getTileEntity(pos);

            LocatedBlock lb = new LocatedBlock(blockState, tileentity, pos);
            assemblyInteractor.blockDisassembled(lb);
            DisassembleBlockEvent event = new DisassembleBlockEvent(this.movingWorld, lb);
            MinecraftForge.EVENT_BUS.post(event);
            this.result.assembleBlock(lb);
        }

        return retPostList;
    }

    private LocatedBlock rotateBlock(LocatedBlock locatedBlock, int deltaRot) {
        deltaRot &= 3;
        if (deltaRot != 0) {
            for (int r = 0; r < deltaRot; r++) {
                locatedBlock = RotationHelper.INSTANCE.rotateBlock(locatedBlock, true);
            }
        }
        return locatedBlock;
    }
}
