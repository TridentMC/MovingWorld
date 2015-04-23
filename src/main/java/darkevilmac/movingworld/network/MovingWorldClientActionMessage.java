package darkevilmac.movingworld.network;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

public class MovingWorldClientActionMessage extends EntityMovingWorldMessage {

    public enum Action {
        NONE, ALIGN, DISASSEMBLE, DISASSEMBLEOVERWRITE;

        public int toInt(Action action) {
            switch (action) {
                case ALIGN:
                    return 1;
                case DISASSEMBLE:
                    return 2;
                case DISASSEMBLEOVERWRITE:
                    return 3;
                default:
                    return 0;
            }
        }

        public Action fromInt(int actionInt) {
            switch (actionInt) {
                case 1:
                    return ALIGN;
                case 2:
                    return DISASSEMBLE;
                case 3:
                    return DISASSEMBLEOVERWRITE;
                default:
                    return NONE;
            }
        }

    }

    public Action actionID;

    public MovingWorldClientActionMessage() {
        super();
        actionID = Action.NONE;
    }

    public MovingWorldClientActionMessage(EntityMovingWorld movingWorld, Action id) {
        super(movingWorld);
        actionID = id;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf) {
        super.encodeInto(ctx, buf);
        buf.writeByte(actionID.toInt(actionID));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, EntityPlayer player) {
        super.decodeInto(ctx, buf, player);
        actionID = Action.NONE;
        actionID = actionID.fromInt(buf.readByte());
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (movingWorld != null && movingWorld.riddenByEntity == player) {
            switch (actionID) {
                case DISASSEMBLE:
                    movingWorld.alignToGrid();
                    movingWorld.updateRiderPosition(player, movingWorld.riderDestinationX, movingWorld.riderDestinationY, movingWorld.riderDestinationZ, 1);
                    movingWorld.disassemble(false);
                    break;
                case DISASSEMBLEOVERWRITE:
                    movingWorld.alignToGrid();
                    movingWorld.updateRiderPosition(player, movingWorld.riderDestinationX, movingWorld.riderDestinationY, movingWorld.riderDestinationZ, 1);
                    movingWorld.disassemble(true);
                    break;
                case ALIGN:
                    movingWorld.alignToGrid();
                    break;
                default:
                    break;
            }
        }
    }
}
