package darkevilmac.movingworld.common.core.factory;


import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.MovingWorldManager;
import darkevilmac.movingworld.common.core.MovingWorldProvider;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/**
 * Used to easily create a MovingWorld without a bunch of casting crap to hook into all the mixins.
 */
public class CommonMovingWorldFactory {

    // Some hacky way of getting these variables into a MovingWorld before construction is complete, they can be here because they constantly get overwritten.
    public Integer currentID = null;
    public World currentParent = null;

    public IMovingWorld createMovingWorld(BlockMap blockMap, World parent) {
        if (parent != null && parent instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) parent;
            IWorldMixin mixedWorldServer = (IWorldMixin) worldServer;

            return mixedWorldServer.createMovingWorld(blockMap).getKey().setParent(parent);

            //TODO: Send packet to client to notify that there's a new MovingWorld that's been created.
        }
        return null;
    }

    public void setFactoryVariables(Integer currentID, World currentParent) {
        this.currentID = currentID;
        this.currentParent = currentParent;
    }

    /**
     * Loads an existing movingworld into the parent from file.
     *
     * @param parent
     * @param child
     */
    public void loadMovingWorld(World parent, Integer child) {
        if (parent != null && parent instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) parent;
            IWorldMixin mixedWorldServer = (IWorldMixin) worldServer;

            if (!DimensionManager.isDimensionRegistered(child)) {
                DimensionManager.registerDimension(child, MovingWorldProvider.PROVIDERID);
            } else {
                MovingWorldManager.movingWorldIDS.get(parent.provider.getDimensionId()).remove(new Integer(child));
                MovingWorldManager.registerMovingWorld(parent.provider.getDimensionId(), DimensionManager.getNextFreeDimId());
            }

            mixedWorldServer.loadMovingWorld(parent, child);
        }
    }
}
