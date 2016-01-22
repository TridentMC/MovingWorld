package darkevilmac.movingworld.common.asm.mixin.world;


import com.google.common.collect.Lists;
import darkevilmac.movingworld.common.core.IWorldMixin;
import darkevilmac.movingworld.common.core.SubWorld;
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
    private final List<TileEntity> addedTileEntityList = Lists.<TileEntity>newArrayList();
    @Shadow
    private final List<TileEntity> tileEntitiesToBeRemoved = Lists.<TileEntity>newArrayList();
    @Shadow
    private boolean processingLoadedTiles;

    public HashMap<String /*ID*/, SubWorld> subworlds;

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
    public List<SubWorld> getSubWorlds() {
        return Lists.newArrayList(subworlds.values());
    }

    @Override
    public SubWorld getSubWorldById(String id) {
        return subworlds.get(id);
    }

    @Inject(method = "updateEntities", at = @At("RETURN"))
    public void onUpdateEntities(CallbackInfo cbi) {
        for (SubWorld sub : subworlds.values()) {
            sub.updateEntities();
        }
    }

}
