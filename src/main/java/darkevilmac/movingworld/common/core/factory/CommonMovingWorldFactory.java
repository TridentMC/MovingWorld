package darkevilmac.movingworld.common.core.factory;


import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.assembly.BlockMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Used to easily create a MovingWorld without a bunch of casting crap to hook into all the mixins.
 */
public class CommonMovingWorldFactory {

    // Some hacky way of getting these variables into a MovingWorld before construction is complete, they can be here because they constantly get overwritten.
    public Integer currentID = null;
    public World currentParent = null;

    public void createMovingWorld(BlockMap blockMap, World within) {
        if (within != null && within instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) within;
            IWorldMixin mixedWorldServer = (IWorldMixin) worldServer;

            mixedWorldServer.createMovingWorld(blockMap).getKey().setParent(within);

            //TODO: Send packet to client to notify that there's a new MovingWorld that's been created.
        }
    }

    public void setFactoryVariables(Integer currentID, World currentParent) {
        this.currentID = currentID;
        this.currentParent = currentParent;
    }

}
