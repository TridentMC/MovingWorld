package darkevilmac.movingworld.asm.mixin.world;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(World.class)
public class MixinWorld {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    public int onGetNext(Random rand, int in) {
        onConstruct();

        return rand.nextInt(in);
    }

    public World getThisWorld() {
        return (World) (Object) this;
    }

    public void onConstruct() {
    }
}
