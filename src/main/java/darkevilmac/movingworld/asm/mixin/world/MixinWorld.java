package darkevilmac.movingworld.asm.mixin.world;

import com.google.common.collect.Lists;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(World.class)
public class MixinWorld {

    private static final String EVENT_LISTENERS_FIELD = "Lnet/minecraft/world/World;eventListeners:Ljava/util/List;";

    @Shadow
    protected List<IWorldEventListener> eventListeners;

    @Shadow
    protected PathWorldListener pathListener = new PathWorldListener();

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = EVENT_LISTENERS_FIELD, opcode = Opcodes.PUTFIELD))
    public void onSetEventListeners(World theWorld, List<IWorldEventListener> theList) {
        onConstruct();
        this.eventListeners = Lists.newArrayList(new IWorldEventListener[]{pathListener});
    }

    public void onConstruct() {
        // I was put here to be overridden, don't hurt me anymore please.
    }


}
