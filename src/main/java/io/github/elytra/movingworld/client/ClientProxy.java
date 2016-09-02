package io.github.elytra.movingworld.client;

import io.github.elytra.movingworld.common.CommonProxy;
import io.github.elytra.movingworld.common.config.MainConfig;

public class ClientProxy extends CommonProxy {

    public MainConfig syncedConfig;

    @Override
    public void registerRenderers() {
    }
}
