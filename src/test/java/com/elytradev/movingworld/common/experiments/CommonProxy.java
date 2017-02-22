package com.elytradev.movingworld.common.experiments;

public class CommonProxy {

    private MovingWorldCommonDatabase commonDatabase;

    public void registerRenders() {
    }

    public void setupDB(){
        commonDatabase = new MovingWorldCommonDatabase();
    }

    public IMovingWorldDB getDB() {
        return commonDatabase;
    }
}
