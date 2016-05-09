package elytra.movingworld.common.core.assembly;

/**
 * Created by DarkEvilMac on 1/30/2016.
 */


public abstract class AssemblyInteractor {
    /**
     * Should the flood fill go diagonal, or just to it's direct NSWE neighbors?
     * <p/>
     * If you're confused please see here: https://en.wikipedia.org/wiki/Flood_fill
     * <p/>
     * 4 directions is most similar to false, 8 directions is most similar to true.
     */
    public abstract boolean doDiagonal();

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

    /**
     * Once we reach this many blocks found, or we're unable to find any more we stop iterating and set complete to true.
     */
    public abstract int maxSize();
}