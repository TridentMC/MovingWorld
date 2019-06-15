package com.tridevmc.movingworld.common.network.message;


import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.tile.TileMovingMarkingBlock;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
    public void handle(PlayerEntity sender) {
        if (movingWorld == null)
            return;

        CompoundNBT tagCompound = new CompoundNBT();
        ListNBT list = new ListNBT();
        for (TileEntity te : movingWorld.getMobileChunk().chunkTileEntityMap.values()) {
            CompoundNBT nbt = new CompoundNBT();
            if (te instanceof TileMovingMarkingBlock) {
                ((TileMovingMarkingBlock) te).writeNBTForSending(nbt);
            } else {
                te.write(nbt);
            }
            list.add(nbt);
        }
        tagCompound.put("list", list);

        if (sender instanceof ServerPlayerEntity)
            new MovingWorldTileChangeMessage(movingWorld, tagCompound).sendTo((ServerPlayerEntity) sender);
    }
}
