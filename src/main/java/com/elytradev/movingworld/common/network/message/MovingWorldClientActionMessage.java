package com.elytradev.movingworld.common.network.message;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.entity.EntityMovingWorld;
import com.elytradev.movingworld.common.network.MovingWorldClientAction;
import com.elytradev.movingworld.common.network.MovingWorldNetworking;
import com.elytradev.movingworld.common.network.marshallers.EntityMarshaller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by darkevilmac on 1/29/2017.
 */
@ReceivedOn(Side.SERVER)
public class MovingWorldClientActionMessage extends Message {

    @MarshalledAs(EntityMarshaller.MARSHALLER_NAME)
    public EntityMovingWorld movingWorld;
    public MovingWorldClientAction action;

    public MovingWorldClientActionMessage(NetworkContext ctx) {
        super(ctx);
    }

    public MovingWorldClientActionMessage(EntityMovingWorld movingWorld, MovingWorldClientAction action) {
        super(MovingWorldNetworking.NETWORK);
        this.movingWorld = movingWorld;
        this.action = action;

        if (action == MovingWorldClientAction.DISASSEMBLE || action == MovingWorldClientAction.DISASSEMBLEWITHOVERWRITE) {
            movingWorld.disassembling = true;
        }
    }

    @Override
    protected void handle(EntityPlayer sender) {
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
