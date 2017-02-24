package com.elytradev.movingworld.common.experiments;

import net.minecraftforge.common.DimensionManager;

public class CommonProxy {

    public void registerRenders() {
    }

    public void setupDBS() {
    }

    public IMovingWorldDB getCommonDB() {
        return DimensionManager::getWorld;
    }

    public IMovingWorldDB getClientDB() {
        return null;
    }
}
