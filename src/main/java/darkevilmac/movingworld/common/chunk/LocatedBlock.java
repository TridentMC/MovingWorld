package darkevilmac.movingworld.common.chunk;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class LocatedBlock {
    public final Block block;
    public final int blockMeta;
    public final TileEntity tileEntity;
    public final ChunkPosition coords;
    public final ChunkPosition coordsNoOffset;

    public LocatedBlock(Block block, int meta, ChunkPosition coords) {
        this(block, meta, null, coords, null);
    }

    public LocatedBlock(Block block, int meta, TileEntity tileentity, ChunkPosition coords, ChunkPosition coordsNoOffset) {
        this.block = block;
        blockMeta = meta;
        tileEntity = tileentity;
        this.coords = coords;
        this.coordsNoOffset = coordsNoOffset;
    }

    public LocatedBlock(NBTTagCompound comp, World world) {
        block = Block.getBlockById(comp.getInteger("block"));
        blockMeta = comp.getInteger("meta");
        coords = new ChunkPosition(comp.getInteger("x"), comp.getInteger("y"), comp.getInteger("z"));
        coordsNoOffset = null;
        tileEntity = world == null ? null : world.getTileEntity(coords.chunkPosX, coords.chunkPosY, coords.chunkPosZ);
    }

    @Override
    public String toString() {
        return new StringBuilder("LocatedBlock [block=").append(block).append(", meta=").append(blockMeta).append(", coords=[").append(coords.chunkPosX).append(", ").append(coords.chunkPosY).append(", ").append(coords.chunkPosZ).append("]]").toString();
    }

    public void writeToNBT(NBTTagCompound comp) {
        comp.setShort("block", (short) Block.getIdFromBlock(block));
        comp.setInteger("meta", blockMeta);
        comp.setInteger("x", coords.chunkPosX);
        comp.setInteger("y", coords.chunkPosY);
        comp.setInteger("z", coords.chunkPosZ);
    }
}
