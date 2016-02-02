package darkevilmac.movingworld.common.asm.mixin.world;

import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mixin(World.class)
public class MixinWorld implements IWorldMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    public int onGetNext(Random rand, int in) {
        onConstruct();

        return rand.nextInt(in);
    }

    public World getThisWorld() {
        return (World) (Object) this;
    }

    @Override
    public void onConstruct() {
    }

    @Override
    public boolean isMovingWorld() {
        return this instanceof IMovingWorld;
    }

    @Override
    public IMovingWorld createMovingWorld(UUID uuid, BlockMap contents) {
        return null;
    }

    @Override
    public Pair<IMovingWorld, UUID> createMovingWorld(BlockMap contents) {
        return null;
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
