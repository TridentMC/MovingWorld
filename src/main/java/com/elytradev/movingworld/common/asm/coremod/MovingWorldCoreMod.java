package com.elytradev.movingworld.common.asm.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion(value = "1.11")
public class MovingWorldCoreMod implements IFMLLoadingPlugin {

    public MovingWorldCoreMod() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.movingworld.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return "io.github.elytra.movingworld.common.asm.coremod.MovingWorldModContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return "io.github.elytra.movingworld.common.asm.coremod.MovingWorldAccessTransformer";
    }

}
