package elytra.movingworld.asm.mixin.world;

import com.google.common.collect.HashBiMap;
import elytra.movingworld.common.baseclasses.world.IWorldMixin;
import elytra.movingworld.common.core.IMovingWorld;
import elytra.movingworld.common.core.assembly.BlockMap;
import elytra.movingworld.common.core.world.MovingWorldServer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldClient.class)
public class MixinWorldClient implements IWorldMixin {

    public HashBiMap<Integer, MovingWorldServer> movingWorlds;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onConstructed(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo cbi) {
        if (isMovingWorld())
            return;

        movingWorlds = HashBiMap.create();
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
    public boolean loadMovingWorld(World parent, Integer id) {
        return false;
    }

    @Override
    public List<IMovingWorld> getMovingWorlds() {
        return null;
    }
}
