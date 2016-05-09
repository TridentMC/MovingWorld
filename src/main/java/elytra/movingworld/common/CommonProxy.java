package elytra.movingworld.common;

import elytra.movingworld.MovingWorldMod;
import elytra.movingworld.common.core.factory.CommonMovingWorldFactory;
import elytra.movingworld.common.core.util.ITickingTask;
import elytra.movingworld.common.handler.CommonTickHandler;
import elytra.movingworld.common.handler.PlayerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState;

public class CommonProxy {
    public void initEvent(LoaderState.ModState state) {

    }

    public void setupFactory() {
        MovingWorldMod.movingWorldFactory = new CommonMovingWorldFactory();
    }

    public void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(new CommonTickHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }

    public void registerRenderers() {
    }

    public void registerTickable(ITickingTask tickBasedIterable) {
        if (tickBasedIterable.side().isServer())
            CommonTickHandler.INSTANCE.registerIterable(tickBasedIterable);
    }
}
