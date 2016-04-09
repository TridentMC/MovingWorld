package darkevilmac.movingworld.common.network;

import com.google.common.base.Objects;
import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class MovingWorldClientActionMessage extends EntityMovingWorldMessage {

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
    public boolean onMainThread() {
        return true;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.encodeInto(ctx, buf, side);
        buf.writeByte(actionID.toInt(actionID));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.decodeInto(ctx, buf, side);
        actionID = Action.NONE;
        actionID = actionID.fromInt(buf.readByte());
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (movingWorld != null && Objects.equal(movingWorld.getControllingPassenger(), player)) {
            switch (actionID) {
                case DISASSEMBLE:
                    movingWorld.alignToGrid();
                    movingWorld.updatePassengerPosition(player, movingWorld.riderDestination, 1);
                    movingWorld.disassemble(false);
                    break;
                case DISASSEMBLEOVERWRITE:
                    movingWorld.alignToGrid();
                    movingWorld.updatePassengerPosition(player, movingWorld.riderDestination, 1);
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

    public enum Action {
        NONE, ALIGN, DISASSEMBLE, DISASSEMBLEOVERWRITE, CHANGESUBMERSEVAL;

        public int toInt(Action action) {
            switch (action) {
                case ALIGN:
                    return 1;
                case DISASSEMBLE:
                    return 2;
                case DISASSEMBLEOVERWRITE:
                    return 3;
                case CHANGESUBMERSEVAL:
                    return 4;
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
                case 4:
                    return CHANGESUBMERSEVAL;
                default:
                    return NONE;
            }
        }

    }
}
