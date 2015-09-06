package darkevilmac.movingworld.common.chunk.assembly;

import darkevilmac.movingworld.MovingWorld;
import darkevilmac.movingworld.common.chunk.LocatedBlock;
import darkevilmac.movingworld.common.chunk.mobilechunk.MobileChunk;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import darkevilmac.movingworld.common.event.DisassembleBlockEvent;
import darkevilmac.movingworld.common.tile.IMovingWorldTileEntity;
import darkevilmac.movingworld.common.tile.TileMovingWorldMarkingBlock;
import darkevilmac.movingworld.common.util.FloodFiller;
import darkevilmac.movingworld.common.util.LocatedBlockList;
import darkevilmac.movingworld.common.util.MathHelperMod;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class ChunkDisassembler {
    public boolean overwrite;
    private EntityMovingWorld movingWorld;
    private AssembleResult result;
    private LocatedBlockList removedFluidBlocks;
    private TileMovingWorldMarkingBlock tileMarker;

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
                    if ((block != null && !block.isAir(world, ix, iy, iz) && !block.getMaterial().isLiquid() && !assemblyInteractor.canOverwriteBlock(block)) || vec.yCoord > world.getActualHeight()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public AssembleResult doDisassemble(MovingWorldAssemblyInteractor assemblyInteractor) {
        tileMarker = null;
        if (movingWorld.getMobileChunk().marker != null && movingWorld.getMobileChunk().marker.tileEntity != null && movingWorld.getMobileChunk().marker.tileEntity instanceof TileMovingWorldMarkingBlock)
            tileMarker = (TileMovingWorldMarkingBlock) movingWorld.getMobileChunk().marker.tileEntity;

        removedFluidBlocks = new LocatedBlockList();
        World world = movingWorld.worldObj;
        MobileChunk chunk = movingWorld.getMovingWorldChunk();
        LocatedBlockList fillableBlocks = new FloodFiller().floodFillMobileChunk(chunk);
        this.result = new AssembleResult();
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

        float ox = -chunk.getCenterX();
        float oy = -chunk.minY(); //Created the normal way, through a ChunkAssembler, this value will always be 0.
        float oz = -chunk.getCenterZ();

        LocatedBlockList lbList = new LocatedBlockList();

        // Get the blocks into a more easily managed list
        Vec3 vec = Vec3.createVectorHelper(0D, 0D, 0D);
        TileEntity tileentity;
        Block block;
        Block owBlock;
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

                    lbList.add(new LocatedBlock(block, meta, tileentity, new ChunkPosition(ix, iy, iz), new ChunkPosition(i, j, k)));
                }
            }
        }

        LocatedBlockList postList = new LocatedBlockList();

        postList = processLocatedBlockList(world, lbList, postList, assemblyInteractor, fillableBlocks, currentRot); // Needs to be threaded

        world.getGameRules().setOrCreateGameRule("doTileDrops", String.valueOf(flag));

        // finish blocks that weren't set due to minecraft limitations
        for (LocatedBlock ilb : postList) {
            ix = ilb.coords.chunkPosX;
            iy = ilb.coords.chunkPosY;
            iz = ilb.coords.chunkPosZ;
            MovingWorld.logger.debug("Post-rejoining block: " + ilb.toString());
            world.setBlock(ix, iy, iz, ilb.block, ilb.blockMeta, 0);
            assemblyInteractor.blockDisassembled(ilb);
            this.result.assembleBlock(ilb);
        }

        if (tileMarker != null) {
            tileMarker.removedFluidBlocks = removedFluidBlocks;
        }

        movingWorld.setDead();

        if (this.result.movingWorldMarkingBlock == null || !assemblyInteractor.isTileMovingWorldMarker(result.movingWorldMarkingBlock.tileEntity)) {
            this.result.resultCode = AssembleResult.RESULT_MISSING_MARKER;
        } else {
            this.result.checkConsistent(world);
        }
        assemblyInteractor.chunkDissasembled(this.result);
        this.result.assemblyInteractor = assemblyInteractor;

        return result;
    }

    LocatedBlockList processLocatedBlockList(World world, LocatedBlockList locatedBlocks, LocatedBlockList postList, MovingWorldAssemblyInteractor assemblyInteractor, LocatedBlockList fillList, int currentRot) {
        LocatedBlockList retPostList = new LocatedBlockList();
        retPostList.addAll(postList);

        TileEntity tileentity;
        Block block;
        int meta;
        Block owBlock;
        int owMeta;
        for (LocatedBlock locatedBlock : locatedBlocks) {
            int i = locatedBlock.coordsNoOffset.chunkPosX;
            int j = locatedBlock.coordsNoOffset.chunkPosY;
            int k = locatedBlock.coordsNoOffset.chunkPosZ;

            int ix = locatedBlock.coords.chunkPosX;
            int iy = locatedBlock.coords.chunkPosY;
            int iz = locatedBlock.coords.chunkPosZ;

            block = locatedBlock.block;
            meta = locatedBlock.blockMeta;
            tileentity = locatedBlock.tileEntity;

            owBlock = world.getBlock(ix, iy, iz);
            owMeta = world.getBlockMetadata(ix, iy, iz);
            if (owBlock != null)
                assemblyInteractor.blockOverwritten(owBlock);

            if (!fillList.containsLBOfPos(locatedBlock.coordsNoOffset)) {
                if (world.getBlock(ix, iy, iz).getMaterial().isLiquid()) {
                    if (!removedFluidBlocks.containsLBOfPos(locatedBlock.coords))
                        removedFluidBlocks.add(new LocatedBlock(owBlock, owMeta, new ChunkPosition(ix, iy, iz)));
                }
                if (!world.setBlock(ix, iy, iz, block, meta, 2) || block != world.getBlock(ix, iy, iz)) {
                    retPostList.add(new LocatedBlock(block, meta, tileentity, new ChunkPosition(ix, iy, iz), null));
                    continue;
                }
                if (block != world.getBlock(ix, iy, iz)) {
                    world.setBlock(ix, iy, iz, block, meta, 2);
                }
            }
            if (tileentity != null) {
                tileentity.xCoord = ix;
                tileentity.yCoord = iy;
                tileentity.zCoord = iz;

                if (tileentity instanceof IMovingWorldTileEntity) {
                    ((IMovingWorldTileEntity) tileentity).setParentMovingWorld(null, i, j, k);
                }

                NBTTagCompound tileTag = new NBTTagCompound();
                tileentity.writeToNBT(tileTag);

                world.setTileEntity(ix, iy, iz, tileentity);
                world.getTileEntity(ix, iy, iz).readFromNBT(tileTag);
                tileentity.validate();
                tileentity = world.getTileEntity(ix, iy, iz);

                if (tileMarker != null && new ChunkPosition(tileMarker.xCoord, tileMarker.yCoord, tileMarker.zCoord)
                        .equals(new ChunkPosition(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord))) {
                    tileMarker = (TileMovingWorldMarkingBlock) tileentity;
                }
            }

            block = world.getBlock(ix, iy, iz);
            meta = world.getBlockMetadata(ix, iy, iz);
            tileentity = world.getTileEntity(ix, iy, iz);

            if (!MovingWorld.instance.metaRotations.hasBlock(block)) {
                assemblyInteractor.blockRotated(block, world, ix, iy, iz, currentRot);
                rotateBlock(block, world, ix, iy, iz, currentRot);
                block = world.getBlock(ix, iy, iz);
                meta = world.getBlockMetadata(ix, iy, iz);
                tileentity = world.getTileEntity(ix, iy, iz);
            }

            LocatedBlock lb = new LocatedBlock(block, meta, tileentity, new ChunkPosition(ix, iy, iz), new ChunkPosition(i, j, k));
            assemblyInteractor.blockDisassembled(lb);
            DisassembleBlockEvent event = new DisassembleBlockEvent(lb);
            MinecraftForge.EVENT_BUS.post(event);
            result.assembleBlock(lb);

        }

        return retPostList;
    }

    private void rotateBlock(Block block, World world, int x, int y, int z, int deltarot) {
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
