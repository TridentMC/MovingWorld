package com.elytradev.movingworld.common.network.message;

import io.github.elytra.concrete.Message;
import io.github.elytra.concrete.NetworkContext;
import io.github.elytra.concrete.annotation.field.MarshalledAs;
import io.github.elytra.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.network.MovingWorldNetworking;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Sends tile entity data in a MobileChunk to clients.
 */
@ReceivedOn(Side.CLIENT)
public class MovingWorldTileChangeMessage extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public EntityMovingWorld movingWorld;
    public NBTTagCompound tileData;

    public MovingWorldTileChangeMessage(NetworkContext ctx) {
        super(ctx);
    }

    public MovingWorldTileChangeMessage(EntityMovingWorld movingWorld, NBTTagCompound tileData) {
        super(MovingWorldNetworking.NETWORK);
        this.movingWorld = movingWorld;
        this.tileData = tileData;
    }

    @Override
    protected void handle(EntityPlayer sender) {
        if (movingWorld == null && tileData == null && movingWorld.getMobileChunk() == null)
            return;

        NBTTagList list = tileData.getTagList("list", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            if (nbt == null) continue;
            int x = nbt.getInteger("x");
            int y = nbt.getInteger("y");
            int z = nbt.getInteger("z");
            BlockPos pos = new BlockPos(x, y, z);
            try {
                TileEntity te = movingWorld.getMobileChunk().getTileEntity(pos);
                if (te != null)
                    te.readFromNBT(nbt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ((MobileChunkClient) movingWorld.getMobileChunk()).getRenderer().markDirty();
    }
}
