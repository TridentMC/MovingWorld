package darkevilmac.movingworld.common.network;

import darkevilmac.movingworld.common.entity.EntityMovingWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class FarInteractMessage extends EntityMovingWorldMessage {
    public EnumHand hand;
    public ItemStack stack;

    public FarInteractMessage() {
        super();
    }

    public FarInteractMessage(EntityMovingWorld movingWorld, ItemStack stack, EnumHand hand) {
        super(movingWorld);

        this.stack = stack;
        this.hand = hand;
    }

    @Override
    public boolean onMainThread() {
        return false;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.encodeInto(ctx, buf, side);

        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeBoolean(hand.equals(EnumHand.MAIN_HAND));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        super.decodeInto(ctx, buf, side);

        this.stack = ByteBufUtils.readItemStack(buf);
        this.hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (movingWorld != null) {
            player.interact(movingWorld, stack, hand);
        }
    }

}
