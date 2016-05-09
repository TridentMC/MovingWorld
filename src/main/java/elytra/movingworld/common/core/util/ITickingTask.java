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

    void doTick(Side side);

    boolean complete(Side side);

    int iterationsPerTick();

}
