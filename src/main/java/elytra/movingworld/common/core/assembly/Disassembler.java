package elytra.movingworld.common.core.assembly;

import elytra.movingworld.common.core.util.ITickingTask;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Please don't use this, just park your SubWorlds.
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

    @Override
    public int iterationsPerTick() {
        return 0;
    }

    public abstract class DisassembyInteractor {

        /**
         * Should we even bother listening to this interactor or is it a stub?
         */
        public abstract boolean useInteraction();

        /**
         * @return How many times can we try to find new blocks on a single minecraft tick?
         */
        public abstract int iterationsPerTick();

        /**
         * Should the assembler register itself to be ticked automatically or are you going to call tick every tick?
         */
        public abstract boolean selfIterate();

    }

}
