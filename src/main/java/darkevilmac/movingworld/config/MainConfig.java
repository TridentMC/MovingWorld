package darkevilmac.movingworld.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class MainConfig {

    private Configuration config;
    public boolean iterativeAlgorithm;
    public boolean diagonalAssembly;

    public void initConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();
        iterativeAlgorithm = config.get(Configuration.CATEGORY_GENERAL, "Use Iterative Algorithm", false).getBoolean();
        diagonalAssembly = config.get(Configuration.CATEGORY_GENERAL, "Assemble Diagonal Blocks NOTE:Can be overridden by mods!", false).getBoolean();
        config.save();
    }

}
