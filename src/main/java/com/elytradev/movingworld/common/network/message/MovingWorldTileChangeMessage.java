package com.elytradev.movingworld.common.network.message;


import com.elytradev.movingworld.common.chunk.mobilechunk.MobileChunkClient;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;


/**
 * Sends tile entity data in a MobileChunk to clients.
 */
@RegisteredMessage(channel = "movingworld", destination = LogicalSide.CLIENT)
public class MovingWorldTileChangeMessage extends Message {

    public EntityMovingWorld movingWorld;
    public NBTTagCompound tileData;

    public MovingWorldTileChangeMessage() {
        super();
    }

    public MovingWorldTileChangeMessage(EntityMovingWorld movingWorld, NBTTagCompound tileData) {
        super();
        this.movingWorld = movingWorld;
        this.tileData = tileData;
    }

    @Override
    public void handle(EntityPlayer sender) {
        if (movingWorld == null || tileData == null
                || movingWorld.getMobileChunk() == null || !(movingWorld.getMobileChunk() instanceof MobileChunkClient))
            return;

        NBTTagList list = tileData.getList("list", 10);
        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound nbt = list.getCompound(i);
            if (nbt == null) continue;
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            BlockPos pos = new BlockPos(x, y, z);
            try {
                TileEntity te = movingWorld.getMobileChunk().getTileEntity(pos);
                if (te != null)
                    te.read(nbt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ((MobileChunkClient) movingWorld.getMobileChunk()).getRenderer().markDirty();
    }
}
