package com.tridevmc.movingworld.common.network.message;


import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

        if (sender instanceof ServerPlayerEntity)
            new MovingWorldTileChangeMessage(movingWorld, true).sendTo((ServerPlayerEntity) sender);
    }
}
