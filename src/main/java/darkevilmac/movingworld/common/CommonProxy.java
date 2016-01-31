package darkevilmac.movingworld.common;

import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.common.core.factory.CommonMovingWorldFactory;
import darkevilmac.movingworld.common.core.util.ITickBasedIterable;
import darkevilmac.movingworld.common.handler.CommonTickHandler;
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
    }

    public void registerRenderers() {
    }

    public void registerTickable(ITickBasedIterable tickBasedIterable) {
        if (tickBasedIterable.side().isServer())
            CommonTickHandler.INSTANCE.registerIterable(tickBasedIterable);
    }
}
