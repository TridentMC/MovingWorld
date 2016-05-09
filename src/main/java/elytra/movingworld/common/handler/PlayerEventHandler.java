package elytra.movingworld.common.handler;


import elytra.movingworld.MovingWorldMod;
import elytra.movingworld.common.network.sync.auto.DimensionSyncMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerEventHandler {

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.isCanceled() || (e.player != null && e.player.worldObj != null && e.player.worldObj.isRemote)) // Better safe than sorry.
            return;

        if (e.player != null && e.player instanceof EntityPlayerMP)
            MovingWorldMod.instance.network.sendTo(new DimensionSyncMessage(), (EntityPlayerMP) e.player);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.isCanceled())
            return;

        if (e.player != null && e.player instanceof EntityPlayerMP) {
            MovingWorldMod.instance.network.sendTo(new DimensionSyncMessage(true), (EntityPlayerMP) e.player);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent e) {
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
    }

}
