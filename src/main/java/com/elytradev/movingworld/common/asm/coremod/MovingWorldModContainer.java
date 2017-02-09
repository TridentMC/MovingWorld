package com.elytradev.movingworld.common.asm.coremod;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class MovingWorldModContainer extends DummyModContainer {

    public MovingWorldModContainer() {
        super(new ModMetadata());

        ModMetadata meta = getMetadata();
        meta.modId = "io.github.elytra.movingworld.common.asm.coremod";
        meta.name = "MovingWorld CORE";
        meta.authorList = Lists.newArrayList("Darkevilmac");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}
