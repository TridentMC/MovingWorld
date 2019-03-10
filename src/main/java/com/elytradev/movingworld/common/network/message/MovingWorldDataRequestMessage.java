package com.elytradev.movingworld.common.network.message;


import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.tile.TileMovingMarkingBlock;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.LogicalSide;

@RegisteredMessage(channel = "movingworld", destination = LogicalSide.SERVER)
public class MovingWorldDataRequestMessage extends Message {

    public EntityMovingWorld movingWorld;

    public MovingWorldDataRequestMessage() {
        super();
    }

    public MovingWorldDataRequestMessage(EntityMovingWorld movingWorld) {
        super();
        this.movingWorld = movingWorld;
    }

    @Override
    public void handle(EntityPlayer sender) {
        if (movingWorld == null)
            return;

        NBTTagCompound tagCompound = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (TileEntity te : movingWorld.getMobileChunk().chunkTileEntityMap.values()) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (te instanceof TileMovingMarkingBlock) {
                ((TileMovingMarkingBlock) te).writeNBTForSending(nbt);
            } else {
                te.write(nbt);
            }
            list.add(nbt);
        }
        tagCompound.put("list", list);

        if (sender instanceof EntityPlayerMP)
            new MovingWorldTileChangeMessage(movingWorld, tagCompound).sendTo((EntityPlayerMP) sender);
    }
}
