package darkevilmac.movingworld.chunk;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.entity.IMovingWorldTileEntity;
import darkevilmac.movingworld.util.MathHelperMod;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
        MobileChunk chunk = movingWorld.getShipChunk();
        float yaw = Math.round(movingWorld.rotationYaw / 90F) * 90F;
        yaw = (float) Math.toRadians(yaw);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a VehicleFiller, this value will always be 0.
        float oz = -chunk.getCenterZ();

        Vec3 vec = Vec3.createVectorHelper(0D, 0D, 0D);
        Block block;
        int ix, iy, iz;
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    if (chunk.isAirBlock(i, j, k)) continue;
                    vec.xCoord = i + ox;
                    vec.yCoord = j + oy;
                    vec.zCoord = k + oz;
                    vec.rotateAroundY(yaw);

                    ix = MathHelperMod.round_double(vec.xCoord + movingWorld.posX);
                    iy = MathHelperMod.round_double(vec.yCoord + movingWorld.posY);
                    iz = MathHelperMod.round_double(vec.zCoord + movingWorld.posZ);

                    block = world.getBlock(ix, iy, iz);
                    if (block != null && !block.isAir(world, ix, iy, iz) && !block.getMaterial().isLiquid() && !assemblyInteractor.canOverwriteBlock(block)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public AssembleResult doDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        World world = movingWorld.worldObj;
        MobileChunk chunk = movingWorld.getShipChunk();
        AssembleResult result = new AssembleResult();
        result.xOffset = Integer.MAX_VALUE;
        result.yOffset = Integer.MAX_VALUE;
        result.zOffset = Integer.MAX_VALUE;

        int currentRot = Math.round(movingWorld.rotationYaw / 90F) & 3;
        int deltarot = (-currentRot) & 3;
        movingWorld.rotationYaw = currentRot * 90F;
        movingWorld.rotationPitch = 0F;
        float yaw = currentRot * MathHelperMod.PI_HALF;

        boolean flag = world.getGameRules().getGameRuleBooleanValue("doTileDrops");
        world.getGameRules().setOrCreateGameRule("doTileDrops", "false");

        List<LocatedBlock> postlist = new ArrayList<LocatedBlock>(4);

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a ChunkAssembler, this value will always be 0.
        float oz = -chunk.getCenterZ();

        Vec3 vec = Vec3.createVectorHelper(0D, 0D, 0D);
        TileEntity tileentity;
        Block block;
        int meta;
        int ix, iy, iz;
        for (int i = chunk.minX(); i < chunk.maxX(); i++) {
            for (int j = chunk.minY(); j < chunk.maxY(); j++) {
                for (int k = chunk.minZ(); k < chunk.maxZ(); k++) {
                    block = chunk.getBlock(i, j, k);
                    meta = chunk.getBlockMetadata(i, j, k);
                    if (block == Blocks.air) {
                        if (meta == 1) continue;
                    } else if (block.isAir(world, i, j, k)) continue;
                    tileentity = chunk.getTileEntity(i, j, k);

                    meta = MovingWorld.instance.metaRotations.getRotatedMeta(block, meta, deltarot);

                    vec.xCoord = i + ox;
                    vec.yCoord = j + oy;
                    vec.zCoord = k + oz;
                    vec.rotateAroundY(yaw);

                    ix = MathHelperMod.round_double(vec.xCoord + movingWorld.posX);
                    iy = MathHelperMod.round_double(vec.yCoord + movingWorld.posY);
                    iz = MathHelperMod.round_double(vec.zCoord + movingWorld.posZ);

                    if (!world.setBlock(ix, iy, iz, block, meta, 2) || block != world.getBlock(ix, iy, iz)) {
                        postlist.add(new LocatedBlock(block, meta, tileentity, new ChunkPosition(ix, iy, iz)));
                        continue;
                    }
                    if (meta != world.getBlockMetadata(ix, iy, iz)) {
                        world.setBlockMetadataWithNotify(ix, iy, iz, meta, 2);
                    }
                    if (tileentity != null) {
                        if (tileentity instanceof IMovingWorldTileEntity) {
                            ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(null, i, j, k);
                        }
                        tileentity.validate();
                        world.setTileEntity(ix, iy, iz, tileentity);
                    }

                    if (!MovingWorld.instance.metaRotations.hasBlock(block)) {
                        //ShipMod.modLog.debug("Forge-rotating block " + Block.blockRegistry.getNameForObject(block));
                        rotateBlock(block, world, assemblyInteractor, ix, iy, iz, currentRot);
                        block = world.getBlock(ix, iy, iz);
                        meta = world.getBlockMetadata(ix, iy, iz);
                        tileentity = world.getTileEntity(ix, iy, iz);
                    }

                    LocatedBlock lb = new LocatedBlock(block, meta, tileentity, new ChunkPosition(ix, iy, iz));
                    assemblyInteractor.blockDisassembled(lb);
                    result.assembleBlock(lb);
                }
            }
        }

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));

        for (LocatedBlock ilb : postlist) {
            ix = ilb.coords.chunkPosX;
            iy = ilb.coords.chunkPosY;
            iz = ilb.coords.chunkPosZ;
            MovingWorld.logger.debug("Post-rejoining block: " + ilb.toString());
            world.setBlock(ix, iy, iz, ilb.block, ilb.blockMeta, 0);
            assemblyInteractor.blockDisassembled(ilb);
            result.assembleBlock(ilb);
        }

        movingWorld.setDead();

        if (result.movingWorldMarkingBlock == null || !assemblyInteractor.isTileMovingWorldMarker(result.movingWorldMarkingBlock.tileEntity)) {
            result.resultCode = AssembleResult.RESULT_MISSING_MARKER;
        } else {
            result.checkConsistent(world);
        }
        assemblyInteractor.chunkDissasembled(result);
        return result;
    }

    private void rotateBlock(Block block, World world, MovingWorldAssemblyInteractor assemblyInteractor, int x, int y, int z, int deltarot) {
        deltarot &= 3;
        if (deltarot != 0) {
            if (deltarot == 3) {
                block.rotateBlock(world, x, y, z, ForgeDirection.UP);
            } else {
                for (int r = 0; r < deltarot; r++) {
                    block.rotateBlock(world, x, y, z, ForgeDirection.DOWN);
                }
            }
        }
    }
}
