package elytra.movingworld.common;

import elytra.movingworld.common.core.util.ITickingTask;
import elytra.movingworld.common.handler.CommonTickHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState;

public class CommonProxy {
    public void initEvent(LoaderState.ModState state) {

    }

    public void setupFactory() {
        //TODO: Factory implementation.
    }

    public void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(new CommonTickHandler());
    }

    public void registerRenderers() {
    }

    public void registerTickable(ITickingTask tickBasedIterable) {
        if (tickBasedIterable.side().isServer())
            CommonTickHandler.INSTANCE.registerIterable(tickBasedIterable);
    }
}
