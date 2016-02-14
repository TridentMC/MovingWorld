package darkevilmac.movingworld.client.handler;


import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldRenderHandler {

    @SubscribeEvent
    public void onWorldRender(RenderWorldEvent event) {
        if (event.isCanceled())
            return;

        if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().theWorld != null) {
            Minecraft mc = Minecraft.getMinecraft();
            World theWorld = mc.theWorld;
        }
    }

    @SubscribeEvent
    public void onWorldRenderLast(RenderWorldLastEvent event) {
        if (event.isCanceled())
            return;
    }


}
