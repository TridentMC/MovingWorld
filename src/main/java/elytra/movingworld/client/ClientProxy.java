package elytra.movingworld.client;

import elytra.movingworld.client.handler.ClientTickHandler;
import elytra.movingworld.common.CommonProxy;
import elytra.movingworld.common.core.util.ITickingTask;
import elytra.movingworld.common.handler.CommonTickHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState;

public class ClientProxy extends CommonProxy {

    public void initEvent(LoaderState.ModState state) {
    }

    public void setupFactory() {
        //TODO: Factory implementation
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
    }

    @Override
    public void registerRenderers() {
    }

    @Override
    public void registerTickable(ITickingTask tickBasedIterable) {
        if (tickBasedIterable.side() == null) {
            ClientTickHandler.INSTANCE.registerIterable(tickBasedIterable);
            CommonTickHandler.INSTANCE.registerIterable(tickBasedIterable);
        }

        if (tickBasedIterable.side().isClient())
            ClientTickHandler.INSTANCE.registerIterable(tickBasedIterable);
        if (tickBasedIterable.side().isServer())
            CommonTickHandler.INSTANCE.registerIterable(tickBasedIterable);
    }

}
