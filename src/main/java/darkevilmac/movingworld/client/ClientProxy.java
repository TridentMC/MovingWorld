package darkevilmac.movingworld.client;

import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.client.handler.ClientTickHandler;
import darkevilmac.movingworld.common.CommonProxy;
import darkevilmac.movingworld.common.core.factory.ClientMovingWorldFactory;
import darkevilmac.movingworld.common.core.util.ITickBasedIterable;
import darkevilmac.movingworld.common.handler.CommonTickHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState;

public class ClientProxy extends CommonProxy {

    public void initEvent(LoaderState.ModState state) {
    }

    public void setupFactory() {
        MovingWorldMod.movingWorldFactory = new ClientMovingWorldFactory();
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
    public void registerTickable(ITickBasedIterable tickBasedIterable) {
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
