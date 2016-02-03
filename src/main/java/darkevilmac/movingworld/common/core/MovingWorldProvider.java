package darkevilmac.movingworld.common.core;

import net.minecraft.world.WorldProvider;

public class MovingWorldProvider extends WorldProvider {

    public static int PROVIDERID;

    @Override
    public String getDimensionName() {
        return "MovingWorld";
    }

    @Override
    public String getInternalNameSuffix() {
        return "_moving";
    }

    @Override
    public String getSaveFolder() {
        return parentProvider().getSaveFolder();
    }

    public WorldProvider parentProvider() {
        return movingWorld().parent().provider;
    }

    public IMovingWorld movingWorld() {
        return (IMovingWorld) worldObj;
    }
}
