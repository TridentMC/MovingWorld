package darkevilmac.movingworld.network;

import darkevilmac.movingworld.entity.EntityMovingWorld;
import darkevilmac.movingworld.network.advanced.MsgTileEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.swing.text.html.parser.Entity;

/**
 * Created by DarkEvilMac on 4/17/2015.
 */

public class RequestMovingWorldDataMessage extends EntityMovingWorldMessage {

    public RequestMovingWorldDataMessage() {
        super();
    }

    public RequestMovingWorldDataMessage(EntityMovingWorld movingWorld) {
        super(movingWorld);
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (movingWorld != null) {
            if (movingWorld.getMovingWorldChunk().chunkTileEntityMap.isEmpty()) {
                return;
            }

            MsgTileEntities msg = new MsgTileEntities(movingWorld);
            ArchimedesShipMod.instance.pipeline.sendTo(msg, (EntityPlayerMP) player);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }
}
