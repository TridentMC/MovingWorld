package com.elytradev.movingworld.common.experiments.mixin;

import com.elytradev.movingworld.common.experiments.IWorldMixin;
import com.elytradev.movingworld.common.experiments.RegionPool;
import net.minecraft.entity.EntityTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by darkevilmac on 2/13/2017.
 */
@Mixin(WorldServer.class)
public class MixinWorldServer implements IWorldMixin {

    private static final String WORLD_SERVER_CONSTRUCTOR = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/storage/ISaveHandler;Lnet/minecraft/world/storage/WorldInfo;ILnet/minecraft/profiler/Profiler;)V";
    private static final String DIM_MANAGER_SETWORLD = "Lnet/minecraftforge/common/DimensionManager;setWorld(ILnet/minecraft/world/WorldServer;Lnet/minecraft/server/MinecraftServer;)V";

    @Redirect(method = WORLD_SERVER_CONSTRUCTOR, at = @At(value = "INVOKE", target = DIM_MANAGER_SETWORLD))
    private void setDimensionManagerWorld(int dimensionId, WorldServer world, MinecraftServer mcServer) {
        String result = "skipped.";

        if (RegionPool.getPool(dimensionId, false) == null) {
            DimensionManager.setWorld(dimensionId, world, mcServer);
            result = "done.";
        }

        System.out.println("Set dimension from WorldServer instantiation, " + result);
    }

    @Redirect(method = WORLD_SERVER_CONSTRUCTOR,
            at = @At(value = "NEW", args = "class=net/minecraft/entity/EntityTracker"))
    public EntityTracker createEntityTracker(WorldServer thisWorld) {
        onInstantiate(thisWorld.provider.getDimension());

        return new EntityTracker(thisWorld);
    }

    @Override
    public void onInstantiate(int dimension) {

    }

    public WorldServer thisWorld() {
        return (WorldServer) ((Object) this);
    }
}
