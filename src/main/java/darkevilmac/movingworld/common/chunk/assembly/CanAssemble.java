package darkevilmac.movingworld.common.chunk.assembly;

public class CanAssemble {

    public boolean assembleThenCancel;
    public boolean justCancel;

    public CanAssemble(boolean assembleThenCancel, boolean justCancel) {
        this.assembleThenCancel = assembleThenCancel;
        this.justCancel = justCancel;
    }

}
