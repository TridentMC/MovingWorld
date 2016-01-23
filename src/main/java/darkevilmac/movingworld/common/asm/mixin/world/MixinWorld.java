package darkevilmac.movingworld.common.asm.mixin.world;


import com.google.common.collect.Lists;
import darkevilmac.movingworld.common.core.IWorldMixin;
import darkevilmac.movingworld.common.core.MovingWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

@Mixin(World.class)
public class MixinWorld implements IWorldMixin {

    @Shadow
    private final List<TileEntity> addedTileEntityList = Lists.newArrayList();
    @Shadow
    private final List<TileEntity> tileEntitiesToBeRemoved = Lists.newArrayList();
    public HashMap<String /*ID*/, MovingWorld> subworlds;
    @Shadow
    private boolean processingLoadedTiles;

    @Override
    public List<TileEntity> getAddedTileEntityList() {
        return addedTileEntityList;
    }

    @Override
    public List<TileEntity> getTileEntitiesToBeRemoved() {
        return tileEntitiesToBeRemoved;
    }

    @Override
    public boolean isProcessingLoadedTiles() {
        return processingLoadedTiles;
    }

    @Override
    public void setProcessingLoadedTiles(boolean flag) {
        processingLoadedTiles = flag;
    }


    @Override
    public List<MovingWorld> getSubWorlds() {
        return Lists.newArrayList(subworlds.values());
    }

    @Override
    public MovingWorld getSubWorldById(String id) {
        return subworlds.get(id);
    }

    @Inject(method = "updateEntities", at = @At("RETURN"))
    public void onUpdateEntities(CallbackInfo cbi) {
        if (subworlds != null)
            for (MovingWorld sub : subworlds.values()) {
                sub.updateEntities();
            }
    }

}
