package darkevilmac.movingworld.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import darkevilmac.movingworld.block.TileMovingWorldMarkingBlock;
import darkevilmac.movingworld.chunk.MobileChunkClient;
import darkevilmac.movingworld.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TileEntitiesMessage extends EntityMovingWorldMessage {
    private NBTTagCompound tagCompound;

    public TileEntitiesMessage() {
        super();
        tagCompound = null;
    }

    public TileEntitiesMessage(EntityMovingWorld movingWorld) {
        super(movingWorld);
        tagCompound = null;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf) {
        super.encodeInto(ctx, buf);
        tagCompound = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (TileEntity te : movingWorld.getMovingWorldChunk().chunkTileEntityMap.values()) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (te instanceof TileMovingWorldMarkingBlock) {
                ((TileMovingWorldMarkingBlock) te).writeNBTforSending(nbt);
            } else {
                te.writeToNBT(nbt);
            }
            list.appendTag(nbt);
        }
        tagCompound.setTag("list", list);
        DataOutputStream out = new DataOutputStream(new ByteBufOutputStream(buf));
        // oh this is why there was an ioexception.... whatever
        try {
            CompressedStreamTools.write(tagCompound, out);
            out.flush();
        } catch (IOException e) {
            try {
                throw e;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player) {
        if (movingWorld != null) {
            DataInputStream in = new DataInputStream(new ByteBufInputStream(buf));
            try {
                tagCompound = CompressedStreamTools.read(in);
            } catch (IOException e) {
                try {
                    throw e;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player) {
        if (movingWorld != null && tagCompound != null) {
            NBTTagList list = tagCompound.getTagList("list", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt = list.getCompoundTagAt(i);
                if (nbt == null) continue;
                int x = nbt.getInteger("x");
                int y = nbt.getInteger("y");
                int z = nbt.getInteger("z");
                try {
                    TileEntity te = movingWorld.getMovingWorldChunk().getTileEntity(x, y, z);
                    if (te != null)
                        te.readFromNBT(nbt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ((MobileChunkClient) movingWorld.getMovingWorldChunk()).getRenderer().markDirty();
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
    }

}
