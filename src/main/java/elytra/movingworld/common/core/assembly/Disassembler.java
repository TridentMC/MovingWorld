package elytra.movingworld.common.core.assembly;

import elytra.movingworld.common.core.util.ITickingTask;
import net.minecraftforge.fml.relauncher.Side;

/**
 * TODO: Maybe implement, maybe don't.
 */
public class Disassembler implements ITickingTask {

    @Override
    public Side side() {
        return null;
    }

    @Override
    public boolean begin(Side side) {
        return false;
    }

    @Override
    public void doTick(Side side) {
    }

    @Override
    public boolean complete(Side side) {
        return false;
    }

}
