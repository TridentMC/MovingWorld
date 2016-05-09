package elytra.movingworld.common.core.util;

import net.minecraftforge.fml.relauncher.Side;

public interface ITickingTask {

    /**
     * @Return null if to be run on both sides.
     */
    Side side();

    /**
     * @Return true if we should just start ticking now, false if ticking should start on the next tick.
     */
    boolean begin(Side side);

    /**
     * Called 20 times per second just like most thing in minecraft.
     *
     * @param side
     */
    void doTick(Side side);

    /**
     * If we're finished yet, if we are this will be removed from the list of things to iterate and forgotten about.
     *
     * @param side
     * @return
     */
    boolean complete(Side side);

}
