package darkevilmac.movingworld.common.test;


import darkevilmac.movingworld.common.core.assembly.AssemblyInteractor;

public class CustomAssemblyInteractor extends AssemblyInteractor {
    @Override
    public boolean doDiagonal() {
        return false;
    }

    @Override
    public boolean useInteraction() {
        return true;
    }

    @Override
    public int iterationsPerTick() {
        return 25;
    }

    @Override
    public boolean selfIterate() {
        return false;
    }

    @Override
    public int maxSize() {
        return 64;
    }
}
