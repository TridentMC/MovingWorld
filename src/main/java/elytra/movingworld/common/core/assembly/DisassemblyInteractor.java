package elytra.movingworld.common.core.assembly;

/**
 * Created by DarkEvilMac on 5/9/2016.
 */

public abstract class DisassemblyInteractor {

    /**
     * Should we even bother listening to this interactor or is it a stub?
     */
    public abstract boolean useInteraction();

    /**
     * @return How many times can we try to find add blocks back to the parent on a single minecraft tick?
     */
    public abstract int iterationsPerTick();

    /**
     * Should the assembler register itself to be ticked automatically or are you going to call tick every tick?
     */
    public abstract boolean selfIterate();

}
