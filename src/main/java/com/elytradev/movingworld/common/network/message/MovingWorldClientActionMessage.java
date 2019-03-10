package com.elytradev.movingworld.common.network.message;


import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.network.MovingWorldClientAction;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.LogicalSide;


@RegisteredMessage(channel = "movingworld", destination = LogicalSide.SERVER)
public class MovingWorldClientActionMessage extends Message {

    public EntityMovingWorld movingWorld;
    public MovingWorldClientAction action;

    public MovingWorldClientActionMessage() {
        super();
    }

    public MovingWorldClientActionMessage(EntityMovingWorld movingWorld, MovingWorldClientAction action) {
        super();
        this.movingWorld = movingWorld;
        this.action = action;

        if (action == MovingWorldClientAction.DISASSEMBLE || action == MovingWorldClientAction.DISASSEMBLEWITHOVERWRITE) {
            movingWorld.disassembling = true;
        }
    }

    @Override
    public void handle(EntityPlayer sender) {
        if (movingWorld == null || sender != movingWorld.getControllingPassenger())
            return;

        switch (action) {
            case DISASSEMBLE:
                movingWorld.alignToGrid(true);
                movingWorld.updatePassengerPosition(sender, movingWorld.riderDestination, 1);
                movingWorld.removePassengers();
                movingWorld.disassemble(false);
                break;
            case DISASSEMBLEWITHOVERWRITE:
                movingWorld.alignToGrid(true);
                movingWorld.updatePassengerPosition(sender, movingWorld.riderDestination, 1);
                movingWorld.removePassengers();
                movingWorld.disassemble(true);
                break;
            case ALIGN:
                movingWorld.alignToGrid(true);
                break;
            default:
                break;
        }
    }

}
