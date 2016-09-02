package io.github.elytra.movingworld.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

import io.github.elytra.movingworld.MovingWorldMod;

public class MovingWorldConfigGUI extends GuiConfig {

    public MovingWorldConfigGUI(GuiScreen parentScreen) {
        super(parentScreen, generateConfigList(), "MovingWorld",
                false, false, GuiConfig.getAbridgedConfigPath(MovingWorldMod.instance.getNetworkConfig().getConfig().toString()));
    }

    public static List<IConfigElement> generateConfigList() {

        ArrayList<IConfigElement> elements = new ArrayList<IConfigElement>();

        for (String name : MovingWorldMod.instance.getNetworkConfig().getConfig().getCategoryNames())
            elements.add(new ConfigElement(MovingWorldMod.instance.getNetworkConfig().getConfig().getCategory(name)));

        return elements;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }

}
