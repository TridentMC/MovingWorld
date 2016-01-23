package darkevilmac.movingworld.common.core;

import net.minecraft.world.WorldProvider;

public class MovingWorldProvider extends WorldProvider {

    public EntityMovingWorld parentEntity;

    @Override
    public String getDimensionName() {
        return "MovingWorld";
    }

    @Override
    public String getInternalNameSuffix() {
        return "_moving";
    }
}
