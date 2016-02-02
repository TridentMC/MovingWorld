package darkevilmac.movingworld.common.core;

import net.minecraft.world.WorldProvider;

import java.io.File;
import java.util.UUID;

public class MovingWorldProvider extends WorldProvider {

    public WorldProvider parentProvider;
    public UUID id;

    public MovingWorldProvider(WorldProvider parentProvider, UUID id) {
        super();
        this.parentProvider = parentProvider;
        this.id = id;
    }

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
        return new File(new File(parentProvider.getSaveFolder(), "MovingWorld"), id.toString()).toString();
    }
}
