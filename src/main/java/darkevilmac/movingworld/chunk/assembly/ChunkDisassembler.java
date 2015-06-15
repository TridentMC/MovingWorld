package darkevilmac.movingworld.chunk.assembly;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.chunk.LocatedBlock;
import darkevilmac.movingworld.chunk.MovingWorldAssemblyInteractor;
import darkevilmac.movingworld.chunk.mobilechunk.MobileChunk;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.event.DisassembleBlockEvent;
import darkevilmac.movingworld.tile.IMovingWorldTileEntity;
import darkevilmac.movingworld.util.LocatedBlockList;
import darkevilmac.movingworld.util.MathHelperMod;
import darkevilmac.movingworld.util.RotationHelper;
import darkevilmac.movingworld.util.Vec3Mod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

public class ChunkDisassembler {
    public boolean overwrite;
    private EntityMovingWorld movingWorld;
    private AssembleResult result;

    public ChunkDisassembler(EntityMovingWorld EntityMovingWorld) {
        movingWorld = EntityMovingWorld;
        overwrite = false;
    }

    public boolean canDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        if (overwrite) {
            return true;
        }
        World world = movingWorld.worldObj;
        MobileChunk chunk = movingWorld.getMovingWorldChunk();
        float yaw = Math.round(movingWorld.rotationYaw / 90F) * 90F;
        yaw = (float) Math.toRadians(yaw);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a VehicleFiller, this value will always be 0.
        float oz = -chunk.getCenterZ();

        Vec3Mod vec;
        IBlockState state;
        Block block;
        BlockPos pos;
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    if (chunk.isAirBlock(new BlockPos(i, j, k))) continue;
                    Vec3Mod vecB = new Vec3Mod(i + ox, j + oy, k + oz);

                    vec = vecB;
                    vec = vec.rotateAroundY(yaw);

                    pos = new BlockPos(MathHelperMod.round_double(vec.xCoord + movingWorld.posX),
                            MathHelperMod.round_double(vec.yCoord + movingWorld.posY),
                            MathHelperMod.round_double(vec.zCoord + movingWorld.posZ));

                    state = world.getBlockState(pos);
                    block = state.getBlock();
                    if (block != null && !block.isAir(world, pos) && !block.getMaterial().isLiquid() && !assemblyInteractor.canOverwriteBlock(block)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public AssembleResult doDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        World world = movingWorld.getEntityWorld();
        MobileChunk chunk = movingWorld.getMovingWorldChunk();
        this.result = new AssembleResult();
        result.offset = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        int currentRot = Math.round(movingWorld.rotationYaw / 90F);
        movingWorld.rotationYaw = currentRot * 90F;
        movingWorld.rotationPitch = 0F;
        float yaw = currentRot * MathHelperMod.PI_HALF;

        boolean flag = world.getGameRules().getGameRuleBooleanValue("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        LocatedBlockList postList = new LocatedBlockList(4);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a ChunkAssembler, this value will always be 0.
        float oz = -chunk.getCenterZ();

        LocatedBlockList lbList = new LocatedBlockList();

        Vec3Mod vec;
        TileEntity tileentity;
        IBlockState blockState;
        BlockPos pos;
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    blockState = chunk.getBlockState(new BlockPos(i, j, k));
                    if (blockState.getBlock() == Blocks.air) {
                        if (blockState.getBlock().getMetaFromState(blockState) == 1) continue;
                    } else if (blockState.getBlock().isAir(world, new BlockPos(i, j, k))) continue;
                    tileentity = chunk.getTileEntity(new BlockPos(i, j, k));

                    vec = new Vec3Mod(i + ox, j + oy, k + oz);
                    vec = vec.rotateAroundY(yaw);

                    pos = new BlockPos(MathHelperMod.round_double(vec.xCoord + movingWorld.posX),
                            MathHelperMod.round_double(vec.yCoord + movingWorld.posY),
                            MathHelperMod.round_double(vec.zCoord + movingWorld.posZ));

                    lbList.add(new LocatedBlock(blockState, tileentity, pos, new BlockPos(i, j, k)));
                }
            }
        }

        ArrayList<LocatedBlockList> separatedLbLists = lbList.getSortedDisassemblyBlocks();

        for (LocatedBlockList locatedBlockList : separatedLbLists) {
            if (locatedBlockList != null && !locatedBlockList.isEmpty()) {
                postList = processLocatedBlockList(world, locatedBlockList, postList, assemblyInteractor, currentRot);
            }
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));

        ArrayList<LocatedBlockList> sortedPostList = postList.getSortedDisassemblyBlocks();

        for (LocatedBlockList pList : sortedPostList) {
            if (pList != null && !pList.isEmpty())
                for (LocatedBlock locatedBlockInstance : pList) {
                    pos = locatedBlockInstance.blockPos;
                    MovingWorld.logger.debug("Post-rejoining block: " + locatedBlockInstance.toString());
                    world.setBlockState(pos, locatedBlockInstance.blockState, 2);
                    assemblyInteractor.blockDisassembled(locatedBlockInstance);
                    DisassembleBlockEvent event = new DisassembleBlockEvent(locatedBlockInstance);
                    MinecraftForge.EVENT_BUS.post(event);
                    this.result.assembleBlock(locatedBlockInstance);
                }
        }

        movingWorld.setDead();

        if (this.result.movingWorldMarkingBlock == null || !assemblyInteractor.isTileMovingWorldMarker(this.result.movingWorldMarkingBlock.tileEntity)) {
            this.result.resultCode = AssembleResult.RESULT_MISSING_MARKER;
        } else {
            result.checkConsistent(world);
        }
        assemblyInteractor.chunkDissasembled(this.result);
        this.result.assemblyInteractor = assemblyInteractor;
        return result;
    }

    LocatedBlockList processLocatedBlockList(World world, LocatedBlockList locatedBlocks, LocatedBlockList postList, MovingWorldAssemblyInteractor assemblyInteractor, int currentRot) {
        TileEntity tileentity;
        IBlockState blockState;
        BlockPos pos;
        IBlockState owBlockState;
        Block owBlock;

        for (LocatedBlock locatedBlock : locatedBlocks) {
            TileEntitySkull skull = null;
            if (locatedBlock.tileEntity != null && locatedBlock.tileEntity instanceof TileEntitySkull)
                skull = (TileEntitySkull) locatedBlock.tileEntity;

            if (skull != null) System.out.println(skull.getSkullRotation());

            locatedBlock = rotateBlock(locatedBlock, currentRot);

            int i = locatedBlock.bPosNoOffset.getX();
            int j = locatedBlock.bPosNoOffset.getY();
            int k = locatedBlock.bPosNoOffset.getZ();

            pos = locatedBlock.blockPos;
            blockState = locatedBlock.blockState;
            tileentity = locatedBlock.tileEntity;

            if (skull != null) System.out.println(skull.getSkullRotation() + " POST");

            blockState = assemblyInteractor.blockRotated(blockState, currentRot);

            owBlockState = world.getBlockState(pos);
            owBlock = owBlockState.getBlock();
            if (owBlock != null)
                assemblyInteractor.blockOverwritten(owBlock);

            if (!world.setBlockState(pos, blockState, 2) || blockState.getBlock() != world.getBlockState(pos).getBlock()) {
                postList.add(new LocatedBlock(blockState, tileentity, pos));
                continue;
            }
            if (blockState != world.getBlockState(pos)) {
                world.setBlockState(pos, blockState, 2);
            }
            if (tileentity != null) {
                if (tileentity instanceof IMovingWorldTileEntity) {
                    ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(new BlockPos(i, j, k), null);
                }
                tileentity.validate();
                world.setTileEntity(pos, tileentity);
            }

            blockState = world.getBlockState(pos);
            tileentity = world.getTileEntity(pos);

            LocatedBlock lb = new LocatedBlock(blockState, tileentity, pos);
            assemblyInteractor.blockDisassembled(lb);
            DisassembleBlockEvent event = new DisassembleBlockEvent(lb);
            MinecraftForge.EVENT_BUS.post(event);
            result.assembleBlock(lb);
        }

        return postList;
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
