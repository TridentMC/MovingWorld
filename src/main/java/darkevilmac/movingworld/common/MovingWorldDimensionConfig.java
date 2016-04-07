package darkevilmac.movingworld.common;

import com.google.common.collect.Maps;
import com.google.gson.InstanceCreator;
import darkevilmac.movingworld.common.core.world.MovingWorldManager;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.reflect.Type;
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
        InstanceCreator<Map.Entry<Integer, ArrayList<Integer>>> mapEntryCreator = new InstanceCreator<Map.Entry<Integer, ArrayList<Integer>>>() {
            @Override
            public Map.Entry<Integer, ArrayList<Integer>> createInstance(Type type) {
                return Maps.immutableEntry(new Integer(0), new ArrayList<Integer>());
            }
        };

        dims.load();
        for (Configuration dimFile : dimensionConfigs.values()) {
            dimFile.load();

            Integer parentDim = new Integer(dimFile.get(Configuration.CATEGORY_GENERAL, "parent", 0, "The parent dimension").getInt());
            int[] children = dimFile.get(Configuration.CATEGORY_GENERAL, "children", 0, "The children for this dimension").getIntList();

            ArrayList<Integer> childrenList = new ArrayList<Integer>();
            for (int child : children) {
                childrenList.add(new Integer(child));
            }

            MovingWorldManager.movingWorldIDS.put(parentDim, childrenList);
            dimFile.save();
        }
        dims.save();

        for (Map.Entry<Integer, ArrayList<Integer>> entry : MovingWorldManager.movingWorldIDS.entrySet()) {
            System.out.println(entry.toString());
        }

    }

    public void saveDimensionManager() {
        dims.load();
        ArrayList<String> dimEntries = new ArrayList<String>();
        dimEntries.addAll(Arrays.asList(dims.get("dimFiles", Configuration.CATEGORY_GENERAL, new String[0], "DON'T TOUCH MANAGES DIMENSION FILES").getStringList()));

        for (Map.Entry<Integer, ArrayList<Integer>> entry : MovingWorldManager.movingWorldIDS.entrySet()) {
            if (!dimEntries.contains(entry.getKey().toString())) {
                dimEntries.add(String.valueOf(entry.getKey()));
            }

            if (!dimensionConfigs.containsKey(entry.getKey())) {
                dimensionConfigs.put(entry.getKey(), new Configuration(new File(movingWorldDim, entry.getKey() + ".cfg")));
            }

            int[] childrenIntArray = new int[entry.getValue().size()];

            for (int index = 0; index < entry.getValue().size(); index++) {
                childrenIntArray[index] = entry.getValue().get(index).intValue();
            }

            dimensionConfigs.get(entry.getKey()).load();
            dimensionConfigs.get(entry.getKey()).get(Configuration.CATEGORY_GENERAL, "parent", entry.getKey(), "The parent dimension").set(entry.getKey().intValue());
            dimensionConfigs.get(entry.getKey()).get(Configuration.CATEGORY_GENERAL, "children", childrenIntArray, "The children for this dimension").set(childrenIntArray);
            dimensionConfigs.get(entry.getKey()).save();
        }

        String[] dimEntriesArray = new String[dimEntries.size()];
        for (int i = 0; i < dimEntries.size(); i++) {
            dimEntriesArray[i] = dimEntries.get(i);
        }

        dims.get("dimFiles", Configuration.CATEGORY_GENERAL, new String[0], "DON'T TOUCH MANAGES DIMENSION FILES").set(dimEntriesArray);
        dims.save();
    }
}
