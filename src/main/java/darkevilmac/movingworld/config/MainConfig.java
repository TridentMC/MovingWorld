package darkevilmac.movingworld.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class MainConfig {

    private Configuration config;

    public void initConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();
        config.save();
    }

}
