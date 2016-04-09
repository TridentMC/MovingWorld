package darkevilmac.movingworld.common.network.sync.auto;

import darkevilmac.movingworld.common.core.world.MovingWorldManager;
import darkevilmac.movingworld.common.network.MovingWorldMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Syncs the MovingWorldManager information with a client.
 */
public class DimensionSyncMessage extends MovingWorldMessage {

    public boolean clear;

    public DimensionSyncMessage(boolean clear) {
        this.clear = clear;
    }

    public DimensionSyncMessage() {
    }

    @Override
    public boolean onMainThread() {
        return true;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        if (side.isClient())
            return;

        if (clear)
            buf.writeInt(-2);

        if (MovingWorldManager.movingWorldIDS.size() == 0) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(MovingWorldManager.movingWorldIDS.size());
            for (Map.Entry<Integer, ArrayList<Integer>> entry : MovingWorldManager.movingWorldIDS.entrySet()) {
                buf.writeInt(entry.getKey());
                buf.writeInt(entry.getValue().size());
                if (entry.getValue().size() != 0) {
                    for (Integer id : entry.getValue()) {
                        buf.writeInt(id);
                    }
                }
            }
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, Side side) {
        if (side.isServer())
            return;

        int flag = buf.readInt();

        if (flag == -1)
            return;

        if (flag == -2) {
            MovingWorldManager.resetMovingWorldManager();
            return;
        }

        HashMap<Integer, ArrayList<Integer>> movingWorldIDS = new HashMap<Integer, ArrayList<Integer>>();

        int count = flag;
        //Assemble entries from the packet.
        for (int i = 0; i < count; i++) {
            int parentID = buf.readInt();
            int childrenSize = buf.readInt();

            ArrayList<Integer> children = new ArrayList<Integer>();

            for (int childrenIndex = 0; childrenIndex < childrenSize; childrenIndex++) {
                children.add(buf.readInt());
            }

            movingWorldIDS.put(parentID, children);
        }

        MovingWorldManager.reload(movingWorldIDS);
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }
}
