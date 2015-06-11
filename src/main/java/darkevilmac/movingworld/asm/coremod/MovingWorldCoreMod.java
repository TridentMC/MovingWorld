package darkevilmac.movingworld.asm.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Map;

public class MovingWorldCoreMod implements IFMLLoadingPlugin {
    public MovingWorldCoreMod() {
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        env.addConfiguration("mixins.movingworld.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return "darkevilmac.movingworld.asm.coremod.ModContainer";
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
        return "darkevilmac.movingworld.asm.coremod.MovingWorldAccessTransformer";
    }
}
