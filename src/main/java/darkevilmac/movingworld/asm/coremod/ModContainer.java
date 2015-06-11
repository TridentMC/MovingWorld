package darkevilmac.movingworld.asm.coremod;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class ModContainer extends DummyModContainer {

    public ModContainer() {
        super(new ModMetadata());

        ModMetadata meta = getMetadata();
        meta.modId = "darkevilmac.movingworld.asm.coremod";
        meta.name = "MovingWorld CORE";
        meta.authorList = Lists.newArrayList("Darkevilmac");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}
