package darkevilmac.movingworld.common;

import darkevilmac.movingworld.common.core.util.ITickBasedIterable;
import darkevilmac.movingworld.common.handler.CommonTickHandler;
import net.minecraftforge.common.MinecraftForge;

public abstract class CommonProxy {
    public void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(new CommonTickHandler());
    }

    public abstract void registerRenderers();

    public void registerTickable(ITickBasedIterable tickBasedIterable) {
        if (tickBasedIterable.side().isServer())
            CommonTickHandler.INSTANCE.registerIterable(tickBasedIterable);
    }
}
