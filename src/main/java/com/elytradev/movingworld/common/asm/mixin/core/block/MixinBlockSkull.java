package com.elytradev.movingworld.common.asm.mixin.core.block;

import com.elytradev.movingworld.api.rotation.IRotationBlock;
import com.elytradev.movingworld.common.chunk.LocatedBlock;
import com.elytradev.movingworld.common.util.RotationHelper;
import net.minecraft.tileentity.TileEntitySkull;

public class MixinBlockSkull implements IRotationBlock {

    @Override
    public LocatedBlock rotate(LocatedBlock locatedBlock, boolean ccw) {
        //if (locatedBlock.tileEntity instanceof TileEntitySkull) {
        //    TileEntitySkull tile = (TileEntitySkull) locatedBlock.tileEntity;
        //    Accessor<Integer> skullRotation = Accessors.findField(TileEntitySkull.class, "skullRotation", "field_145910_i");
        //    int skullRot = skullRotation.get(tile);
        //    for (int i = 0; i < 4; i++) {
        //        skullRot = RotationHelper.rotateInteger(skullRot, 0, 16, ccw);
        //    }
        //    skullRotation.set(tile, skullRot);
        //}

        return locatedBlock;
    }

    @Override
    public boolean fullRotation() {
        return false;
    }
}
