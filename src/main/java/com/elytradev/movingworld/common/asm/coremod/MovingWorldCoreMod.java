package com.elytradev.movingworld.common.asm.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion(value = "1.12")
public class MovingWorldCoreMod implements IFMLLoadingPlugin {

    public MovingWorldCoreMod() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.movingworld.json");
        try {
            if (Class.forName("com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod") != null)
                Mixins.addConfiguration("mixins.movingworld.experiments.json");
        } catch (ClassNotFoundException e) {
            // yum! tasty tasty exceptions :^)
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return "com.elytradev.movingworld.common.asm.coremod.MovingWorldModContainer";
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
        return "com.elytradev.movingworld.common.asm.coremod.MovingWorldAccessTransformer";
    }

}
