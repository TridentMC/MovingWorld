package elytra.movingworld.common.test;


import elytra.movingworld.common.core.assembly.AssemblyInteractor;

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
        return 256;
    }

    @Override
    public boolean selfIterate() {
        return false;
    }

    @Override
    public int maxSize() {
        return 5000;
    }
}
