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
    public IMovingWorld createMovingWorld(Integer id, BlockMap contents) {
        return null;
    }

    @Override
    public Pair<IMovingWorld, Integer> createMovingWorld(BlockMap contents) {
        return null;
    }

    @Override
    public boolean createMovingWorldFromID(Integer id) {
        return false;
    }

    @Override
    public List<IMovingWorld> getMovingWorlds() {
        return null;
    }
}
