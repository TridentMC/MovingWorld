package darkevilmac.movingworld.common;

import com.google.gson.Gson;
import darkevilmac.movingworld.common.core.MovingWorldManager;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts the MovingWorldManager to and from a json string and puts it in a config file to load on start.
 * <p/>
 * This is only done on the server loading, a packet is sent before player connect to sync this info as well.
 * <p/>
 * TODO: Send aforementioned packet.
 */
public class MovingWorldDimensionConfig {

    public File movingWorldDim;
    public Configuration dims;

    public HashMap<Integer, Configuration> dimensionConfigs;

    /**
     * DONT INSTANTIATE ON PREINIT/INIT/POSTINIT, THIS IS FOR THE SERVER ONLY <3
     *
     * @param dimFolder
     */
    public MovingWorldDimensionConfig(File dimFolder) {
        this.movingWorldDim = dimFolder;
        this.dims = new Configuration(new File(dimFolder, "Dimensions.cfg"));

        dimensionConfigs = new HashMap<Integer, Configuration>();

        for (String dimFile : dims.get("dimFiles", Configuration.CATEGORY_GENERAL, new String[0], "DON'T TOUCH MANAGES DIMENSION FILES").getStringList()) {
            dimensionConfigs.put(Integer.valueOf(dimFile), new Configuration(new File(dimFolder, dimFile + ".cfg")));
        }
    }

    public void loadDimensionManager() {
        dims.load();
        for (Configuration dimFile : dimensionConfigs.values()) {
            dimFile.load();
            String dimensionEntryJSON = dimFile.get("val", Configuration.CATEGORY_GENERAL, "", "Converted from an actual class to and from json, leave it alone.").getString();

            MutablePair<Integer, ArrayList<Integer>> dimensionEntry = new Gson().fromJson(dimensionEntryJSON, MutablePair.class);
            MovingWorldManager.movingWorldIDS.put(dimensionEntry.getKey(), dimensionEntry.getValue());
            dimFile.save();
        }
        dims.save();
    }

    public void saveDimensionManager() {
        dims.load();
        ArrayList<String> dimEntries = new ArrayList<String>();
        dimEntries.addAll(Arrays.asList(dims.get("dimFiles", Configuration.CATEGORY_GENERAL, new String[0], "DON'T TOUCH MANAGES DIMENSION FILES").getStringList()));

        for (Map.Entry<Integer, ArrayList<Integer>> entry : MovingWorldManager.movingWorldIDS.entrySet()) {
            if (!dimEntries.contains(entry.getKey().toString())) {
                dimEntries.add(String.valueOf(entry.getKey()));
            }

            String dimensionEntryJSON = new Gson().toJson(new MutablePair<Integer, ArrayList<Integer>>(entry.getKey(), entry.getValue()), MutablePair.class);
            if (!dimensionConfigs.containsKey(entry.getKey())) {
                dimensionConfigs.put(entry.getKey(), new Configuration(new File(movingWorldDim, entry.getKey() + ".cfg")));
            }
            dimensionConfigs.get(entry.getKey()).load();
            dimensionConfigs.get(entry.getKey()).get("val", Configuration.CATEGORY_GENERAL, "", "Converted from an actual class to and from json, leave it alone.").set(dimensionEntryJSON);
            dimensionConfigs.get(entry.getKey()).save();
        }
        dims.get("dimFiles", Configuration.CATEGORY_GENERAL, new String[0], "DON'T TOUCH MANAGES DIMENSION FILES").set((String[]) dimEntries.toArray());
        dims.save();
    }

}
