package darkevilmac.movingworld.common.asm.mixin.world;


import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.UUID;

@Mixin(World.class)
public class MixinWorld implements IWorldMixin {

    @Override
    public boolean isMovingWorld() {
        return this instanceof IMovingWorld;
    }

    @Override
    public void createMovingWorld(UUID uuid, BlockMap contents) {

    }

    @Override
    public void createMovingWorld(BlockMap contents) {

    }

    @Override
    public boolean createMovingWorldFromUUID(UUID uuid) {
        return false;
    }

    @Override
    public List<IMovingWorld> getMovingWorlds() {
        return null;
    }
}
