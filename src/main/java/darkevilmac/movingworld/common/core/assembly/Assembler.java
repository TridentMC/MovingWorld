package darkevilmac.movingworld.common.core.assembly;

/**
 * Uses a flood fill algorithm to create a BlockMap, based on an area in world.
 * <p/>
 * Not a mandatory implementation, but it's nice to have.
 */
public class Assembler {


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
    }
}
