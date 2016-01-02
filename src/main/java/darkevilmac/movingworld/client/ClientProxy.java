package darkevilmac.movingworld.client;

import darkevilmac.movingworld.common.CommonProxy;
import darkevilmac.movingworld.common.config.MainConfig;

public class ClientProxy extends CommonProxy {

    public MainConfig syncedConfig;

    @Override
    public void registerRenderers() {
        //RenderingRegistry.registerEntityRenderingHandler(EntityMovingWorld.class, new RenderMovingWorld());
    }
}
