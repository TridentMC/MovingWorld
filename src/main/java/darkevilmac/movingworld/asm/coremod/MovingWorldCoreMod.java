package darkevilmac.movingworld.asm.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion(value = "1.9")
public class MovingWorldCoreMod implements IFMLLoadingPlugin {

    public MovingWorldCoreMod() {
        MixinBootstrap.init();
        MixinEnvironment.setCompatibilityLevel(MixinEnvironment.CompatibilityLevel.JAVA_7);
        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        env.addConfiguration("mixins.movingworld.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return MovingWorldModContainer.class.getName();
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
        return MovingWorldAccessTransformer.class.getName();
    }

}