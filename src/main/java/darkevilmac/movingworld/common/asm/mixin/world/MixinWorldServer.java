package darkevilmac.movingworld.common.asm.mixin.world;

import com.google.common.collect.HashBiMap;
import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.MovingWorldInfo;
import darkevilmac.movingworld.common.core.MovingWorldSaveHandler;
import darkevilmac.movingworld.common.core.MovingWorldServer;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(WorldServer.class)
public class MixinWorldServer implements IWorldMixin {

    @Shadow
    private MinecraftServer mcServer;

    public HashBiMap<UUID, MovingWorldServer> movingWorlds;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onConstructed(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo cbi) {
        if (isMovingWorld())
            return;

        movingWorlds = HashBiMap.create();
    }

    @Inject(method = "saveAllChunks(ZLnet/minecraft/util/IProgressUpdate;)V", at = @At(value = "INVOKE"))
    /**
     * Just before we save all chunks let's save our MovingWorld chunks.
     */
    public void onSaveAllChunks(boolean log, IProgressUpdate progress, CallbackInfo cbi) {
        if (isMovingWorld())
            return;

        WorldServer thisWorldServer = (WorldServer) (Object) this;
        IWorldMixin worldMixed = (IWorldMixin) thisWorldServer;

        if (worldMixed != null && worldMixed.getMovingWorlds() != null && !worldMixed.getMovingWorlds().isEmpty()) {
            for (IMovingWorld movingWorld : worldMixed.getMovingWorlds()) {
                if (movingWorld instanceof MovingWorldServer) {
                    try {
                        ((MovingWorldServer) movingWorld).saveAllChunks(log, progress);
                    } catch (MinecraftException e) {
                        MovingWorldMod.logger.error(e);
                    }
                }
            }
        }

    }

    @Override
    public void onConstruct() {
    }

    @Override
    public boolean isMovingWorld() {
        return this instanceof IMovingWorld;
    }

    /**
     * Create a movingworld that doesn't currently exist out of a blockmap.
     *
     * @param uuid     a unique identification for this world.
     * @param contents the contents of the blocks you want to create a movingworld out of.
     */
    @Override
    public IMovingWorld createMovingWorld(UUID uuid, BlockMap contents) {
        if (isMovingWorld())
            return null;

        MovingWorldMod.movingWorldFactory.setFactoryVariables(uuid, getThisWorld());

        MovingWorldServer movingWorldServer = new MovingWorldServer(
                mcServer, new MovingWorldSaveHandler(getThisWorld().getSaveHandler(), uuid), new MovingWorldInfo(),
                Byte.MAX_VALUE, getThisWorld().theProfiler, uuid, getThisWorld());
        ((MovingWorldSaveHandler) movingWorldServer.getSaveHandler()).movingWorld = movingWorldServer;

        for (Pair<BlockPos, Pair<IBlockState, TileEntity>> entry : contents) {
            movingWorldServer.setBlockState(entry.getKey(), entry.getValue().getKey());
            movingWorldServer.setTileEntity(entry.getKey(), entry.getValue().getValue());
        }
        movingWorldServer.init();
        movingWorlds.put(uuid, movingWorldServer);
        return movingWorldServer;
    }

    @Override
    public Pair<IMovingWorld, UUID> createMovingWorld(BlockMap contents) {
        if (isMovingWorld())
            return null;

        UUID uuid = UUID.randomUUID();

        return new ImmutablePair<IMovingWorld, UUID>(createMovingWorld(uuid, contents), uuid);
    }

    /**
     * A method called to load previously existing movingworlds into this parent world.
     *
     * @param uuid
     * @return if key existed and world was loaded
     */
    @Override
    public boolean createMovingWorldFromUUID(UUID uuid) {
        if (isMovingWorld())
            return false;

        return false;
    }


    @Override
    public List<IMovingWorld> getMovingWorlds() {
        if (isMovingWorld())
            return null;
        return null;
    }

    public WorldServer getThisWorld() {
        return (WorldServer) (Object) this;
    }
}
