package darkevilmac.movingworld.common.core;

import darkevilmac.movingworld.MovingWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.util.Iterator;

public class SubWorld extends World {

    public EntityMovingWorld parentEntity;
    public World parentWorld;

    public BlockPos ORIGIN = new BlockPos(0, 0, 0);
    public Vec3 WORLDPOSITION;

    public Vec3 min;
    public Vec3 max;

    protected SubWorld(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected int getRenderDistanceChunks() {
        return 0;
    }

    public IWorldMixin getMixedWorld() {
        return (IWorldMixin) this;
    }

    @Override
    public void updateEntities() {
        this.theProfiler.startSection("global");
        this.theProfiler.startSection("blockEntities");
        this.getMixedWorld().setProcessingLoadedTiles(true);
        Iterator<TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext()) {
            TileEntity tickableTile = iterator.next();

            if (!tickableTile.isInvalid() && tickableTile.hasWorldObj()) {
                BlockPos blockpos = tickableTile.getPos();

                if (this.isBlockLoaded(blockpos) && this.getWorldBorder().contains(blockpos)) {
                    try {
                        ((ITickable) tickableTile).update();
                    } catch (Throwable throwable) {
                        CrashReport tickCrashReport = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory tickCrashReportCategory = tickCrashReport.makeCategory("Block entity being ticked");
                        tickableTile.addInfoToCrashReport(tickCrashReportCategory);
                        if (net.minecraftforge.common.ForgeModContainer.removeErroringTileEntities) {
                            MovingWorld.logger.error(tickCrashReport.getCompleteReport());
                            tickableTile.invalidate();
                            this.removeTileEntity(tickableTile.getPos());
                        } else {
                            throw new ReportedException(tickCrashReport);
                        }
                    }
                }
            }

            if (tickableTile.isInvalid()) {
                iterator.remove();
                this.loadedTileEntityList.remove(tickableTile);

                if (this.isBlockLoaded(tickableTile.getPos())) {
                    this.getChunkFromBlockCoords(tickableTile.getPos()).removeTileEntity(tickableTile.getPos());
                }
            }
        }

        if (!this.getMixedWorld().getTileEntitiesToBeRemoved().isEmpty()) {
            for (Object tile : getMixedWorld().getTileEntitiesToBeRemoved()) {
                ((TileEntity) tile).onChunkUnload();
            }

            this.tickableTileEntities.removeAll(this.getMixedWorld().getTileEntitiesToBeRemoved());
            this.loadedTileEntityList.removeAll(this.getMixedWorld().getTileEntitiesToBeRemoved());
            this.getMixedWorld().getTileEntitiesToBeRemoved().clear();
        }

        this.getMixedWorld().setProcessingLoadedTiles(false);  //FML Move below remove to prevent CMEs

        this.theProfiler.endStartSection("pendingBlockEntities");

        if (!this.getMixedWorld().getAddedTileEntityList().isEmpty()) {
            for (int j1 = 0; j1 < this.getMixedWorld().getAddedTileEntityList().size(); ++j1) {
                TileEntity addedTile = this.getMixedWorld().getAddedTileEntityList().get(j1);

                if (!addedTile.isInvalid()) {
                    if (!this.loadedTileEntityList.contains(addedTile)) {
                        this.addTileEntity(addedTile);
                    }

                    if (this.isBlockLoaded(addedTile.getPos())) {
                        this.getChunkFromBlockCoords(addedTile.getPos()).addTileEntity(addedTile.getPos(), addedTile);
                    }

                    this.markBlockForUpdate(addedTile.getPos());
                }
            }

            this.getMixedWorld().getAddedTileEntityList().clear();
        }

        this.theProfiler.endSection();
        this.theProfiler.endSection();
    }

}
