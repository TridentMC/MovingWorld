package darkevilmac.movingworld.common.asm.mixin.server;

import darkevilmac.movingworld.common.baseclasses.world.IWorldMixin;
import darkevilmac.movingworld.common.core.IMovingWorld;
import darkevilmac.movingworld.common.core.MovingWorldServer;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow
    @Final
    Profiler theProfiler;

    @Shadow
    int tickCounter;

    @Shadow
    ServerConfigurationManager serverConfigManager;

    private long lastNanoTime;

    public java.util.Hashtable<Integer, long[]> movingWorldTickTimes = new java.util.Hashtable<Integer, long[]>();

    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target =
            "Ljava/lang/System;nanoTime" + "()" + "J"))
    /**
     * Get the last call to nanotime, call it on our end and store the value for future use. WorldServer still gets the variable and we get it as well.
     */
    public long onGetNanoTime() {
        this.lastNanoTime = System.nanoTime();

        return lastNanoTime;
    }

    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target =
            "Lnet/minecraftforge/common/DimensionManager;getWorld" + "(I)" + "Lnet/minecraft/world/WorldServer;"))
    /**
     * Override DimensionManger.getWorld to iterate over subworlds before iterating over the parent world. Should probably be done the other way but that's not viable.
     */
    public WorldServer onUpdateTimeLightAndEntities(int id) {
        WorldServer worldServer = DimensionManager.getWorld(id);
        IWorldMixin mixedWorld = (IWorldMixin) worldServer;

        if (mixedWorld == null || mixedWorld.getMovingWorlds() == null || mixedWorld.getMovingWorlds().isEmpty())
            return worldServer;

        //TODO: Adjust packet calls and such to properly update a MovingWorld.
        for (IMovingWorld movingWorld : mixedWorld.getMovingWorlds()) {
            System.out.println(movingWorld + " being updated! :D");
            MovingWorldServer movingWorldServer = (MovingWorldServer) movingWorld;

            this.theProfiler.startSection(movingWorldServer.getWorldInfo().getWorldName());

            if (this.tickCounter % 20 == 0) {
                this.theProfiler.startSection("timeSync");
                this.serverConfigManager.sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(movingWorldServer.getTotalWorldTime(), movingWorldServer.getWorldTime(), movingWorldServer.getGameRules().getBoolean("doDaylightCycle")), movingWorldServer.provider.getDimensionId());
                this.theProfiler.endSection();
            }

            this.theProfiler.startSection("tick");
            FMLCommonHandler.instance().onPreWorldTick(movingWorldServer);

            try {
                movingWorldServer.tick();
            } catch (Throwable throwable1) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
                movingWorldServer.addWorldInfoToCrashReport(crashreport);
                throw new ReportedException(crashreport);
            }

            try {
                movingWorldServer.updateEntities();
            } catch (Throwable throwable) {
                CrashReport tickingReport = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                movingWorldServer.addWorldInfoToCrashReport(tickingReport);
                throw new ReportedException(tickingReport);
            }

            FMLCommonHandler.instance().onPostWorldTick(movingWorldServer);
            this.theProfiler.endSection();
            this.theProfiler.startSection("tracker");
            movingWorldServer.getEntityTracker().updateTrackedEntities();
            this.theProfiler.endSection();
            this.theProfiler.endSection();
        }

        movingWorldTickTimes.get(id)[this.tickCounter % 100] = System.nanoTime() - lastNanoTime;

        return worldServer;
    }

}
