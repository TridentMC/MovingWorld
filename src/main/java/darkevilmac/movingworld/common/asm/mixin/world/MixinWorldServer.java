package darkevilmac.movingworld.common.asm.mixin.world;

import com.google.common.collect.HashBiMap;
import darkevilmac.movingworld.MovingWorldMod;
import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.*;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldServer.class)
public class MixinWorldServer implements IWorldMixin {

    public HashBiMap<Integer, MovingWorldServer> movingWorlds;
    @Shadow
    @Final
    private MinecraftServer mcServer;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onConstructed(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn, CallbackInfo cbi) {
        if (isMovingWorld())
            return;

        movingWorlds = HashBiMap.create();
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
     * @param contents the contents of the blocks you want to create a movingworld out of.
     */
    @Override
    public IMovingWorld createMovingWorld(Integer id, BlockMap contents) {
        if (isMovingWorld())
            return null;

        MovingWorldMod.movingWorldFactory.setFactoryVariables(id, getThisWorld());

        DimensionManager.registerDimension(id, MovingWorldProvider.PROVIDERID);

        MovingWorldServer movingWorldServer = new MovingWorldServer(
                mcServer, new MovingWorldSaveHandler(getThisWorld().getSaveHandler(), id), new MovingWorldInfo(getThisWorld().getWorldInfo()),
                id, getThisWorld().theProfiler, id, getThisWorld());

        ((MovingWorldSaveHandler) movingWorldServer.getSaveHandler()).movingWorld = movingWorldServer;

        movingWorldServer.init();

        BlockMap failed = new BlockMap(new Vec3i(0, 0, 0));

        for (Pair<BlockPos, Pair<IBlockState, TileEntity>> entry : contents) {
            System.out.println("Setting a block to movingworld with the following info: " + entry.getLeft() + " " + entry.getRight().getLeft().toString());
            if (!movingWorldServer.setBlockState(entry.getKey(), entry.getValue().getKey())) {
                failed.addToMap(entry.getLeft(), entry.getRight().getLeft(), entry.getRight().getRight());
            }
            movingWorldServer.setTileEntity(entry.getKey(), entry.getValue().getValue());
        }

        for (Pair<BlockPos, Pair<IBlockState, TileEntity>> entry : failed) {
            System.out.println("Setting a failed block to movingworld with the following info: " + entry.getLeft() + " " + entry.getRight().getLeft().toString());
            movingWorldServer.setBlockState(entry.getKey(), entry.getValue().getKey());
            movingWorldServer.setTileEntity(entry.getKey(), entry.getValue().getValue());
        }

        movingWorlds.put(id, movingWorldServer);
        MovingWorldManager.registerMovingWorld(getThisWorld(), movingWorldServer);

        return movingWorldServer;
    }

    @Override
    public Pair<IMovingWorld, Integer> createMovingWorld(BlockMap contents) {
        if (isMovingWorld())
            return null;

        int id = DimensionManager.getNextFreeDimId();

        return new ImmutablePair<IMovingWorld, Integer>(createMovingWorld(id, contents), id);
    }

    /**
     * A method called to load previously existing movingworlds into this parent world.
     *
     * @return if key existed and world was loaded
     */
    @Override
    public boolean createMovingWorldFromID(World parent, Integer id) {
        if (isMovingWorld())
            return false;

        MovingWorldMod.movingWorldFactory.setFactoryVariables(id, getThisWorld());
        DimensionManager.registerDimension(id, MovingWorldProvider.PROVIDERID);

        MovingWorldServer movingWorldServer = new MovingWorldServer(
                mcServer, new MovingWorldSaveHandler(getThisWorld().getSaveHandler(), id), new MovingWorldInfo(getThisWorld().getWorldInfo()),
                id, getThisWorld().theProfiler, id, getThisWorld());

        ((MovingWorldSaveHandler) movingWorldServer.getSaveHandler()).movingWorld = movingWorldServer;

        movingWorldServer.init();

        movingWorlds.put(id, movingWorldServer);

        // No need to register the movingworld if we're loading it.

        return true;
    }


    @Override
    public List<IMovingWorld> getMovingWorlds() {
        if (isMovingWorld())
            return null;
        List<IMovingWorld> movingWorlds = new ArrayList<IMovingWorld>();
        movingWorlds.addAll(this.movingWorlds.values());

        return movingWorlds;
    }

    public WorldServer getThisWorld() {
        return (WorldServer) (Object) this;
    }
}
