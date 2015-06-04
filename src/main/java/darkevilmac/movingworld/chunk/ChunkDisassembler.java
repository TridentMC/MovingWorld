package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.event.DisassembleBlockEvent;
import darkevilmac.movingworld.tile.IMovingWorldTileEntity;
import darkevilmac.movingworld.util.MathHelperMod;
import darkevilmac.movingworld.util.RotationHelper;
import darkevilmac.movingworld.util.Vec3Mod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class ChunkDisassembler {
    public boolean overwrite;
    private EntityMovingWorld movingWorld;

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
        AssembleResult result = new AssembleResult();
        result.offset = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        int currentRot = Math.round(movingWorld.rotationYaw / 90F);
        movingWorld.rotationYaw = currentRot * 90F;
        movingWorld.rotationPitch = 0F;
        float yaw = currentRot * MathHelperMod.PI_HALF;

        boolean flag = world.getGameRules().getGameRuleBooleanValue("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        List<LocatedBlock> postList = new ArrayList<LocatedBlock>(4);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a ChunkAssembler, this value will always be 0.
        float oz = -chunk.getCenterZ();

        Vec3Mod vec;
        TileEntity tileentity;
        IBlockState blockState;
        IBlockState owBlockState;
        Block owBlock;
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


                    assemblyInteractor.blockRotated(blockState.getBlock(), world, pos, currentRot);
                    rotateBlock(world, pos, currentRot);
                    blockState = world.getBlockState(pos);
                    tileentity = world.getTileEntity(pos);


                    LocatedBlock lb = new LocatedBlock(blockState, tileentity, pos);
                    assemblyInteractor.blockDisassembled(lb);
                    DisassembleBlockEvent event = new DisassembleBlockEvent(lb);
                    MinecraftForge.EVENT_BUS.post(event);
                    result.assembleBlock(lb);
                }
            }
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));

        for (LocatedBlock locatedBlockInstance : postList) {
            pos = locatedBlockInstance.blockPos;
            MovingWorld.logger.debug("Post-rejoining block: " + locatedBlockInstance.toString());
            world.setBlockState(pos, locatedBlockInstance.blockState);
            assemblyInteractor.blockDisassembled(locatedBlockInstance);
            DisassembleBlockEvent event = new DisassembleBlockEvent(locatedBlockInstance);
            MinecraftForge.EVENT_BUS.post(event);
            result.assembleBlock(locatedBlockInstance);
        }

        movingWorld.setDead();

        if (result.movingWorldMarkingBlock == null || !assemblyInteractor.isTileMovingWorldMarker(result.movingWorldMarkingBlock.tileEntity)) {
            result.resultCode = AssembleResult.RESULT_MISSING_MARKER;
        } else {
            result.checkConsistent(world);
        }
        assemblyInteractor.chunkDissasembled(result);
        result.assemblyInteractor = assemblyInteractor;
        return result;
    }

    private void rotateBlock(World world, BlockPos pos, int deltaRot) {
        deltaRot &= 3;

        if (deltaRot != 0) {
            {
                for (int r = 0; r < deltaRot + 2; r++) {
                    RotationHelper.rotateBlock(world, pos);
                }
            }
        }
    }
}
