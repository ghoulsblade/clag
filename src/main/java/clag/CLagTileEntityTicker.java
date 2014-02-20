package clag;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ForgeDummyContainer;

import java.util.ArrayList;
import java.util.Iterator;

// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/profiling/EntityTickProfiler.java
public class CLagTileEntityTicker {
    public static CLagTileEntityTicker instance = new CLagTileEntityTicker();

    public CLagTileEntityTicker ()
    {

    }

    public void runTileEntities(World world, ArrayList<TileEntity> toTick) {
        IChunkProvider chunkProvider = world.getChunkProvider();
        Iterator<TileEntity> iterator = toTick.iterator();
        long end = System.nanoTime();
        long start;
        int dim = world.provider.dimensionId;

        boolean bIsProfiling = true;

        while (iterator.hasNext()) {
            TileEntity tileEntity = iterator.next();

            int x = tileEntity.xCoord;
            int z = tileEntity.zCoord;
            int cx = x >> 4;
            int cz = z >> 4;

            if (isChunkSkippedNow(dim, cx,cz)) continue;

            if (!tileEntity.isInvalid() && tileEntity.hasWorldObj() && chunkProvider.chunkExists(cx, cz)) {
                try {
                    tileEntity.updateEntity();
                } catch (Throwable var6) {
                    CrashReport crashReport = CrashReport.makeCrashReport(var6, "Ticking tile entity");
                    CrashReportCategory crashReportCategory = crashReport.makeCategory("Tile entity being ticked");
                    tileEntity.func_85027_a(crashReportCategory);
                    if (ForgeDummyContainer.removeErroringTileEntities) {
                        FMLLog.severe(crashReport.getCompleteReport());
                        tileEntity.invalidate();
                        world.setBlockToAir(x, tileEntity.yCoord, z);
                    } else {
                        throw new ReportedException(crashReport);
                    }
                }
            }

            if (tileEntity.isInvalid()) {
                iterator.remove();

                if (chunkProvider.chunkExists(tileEntity.xCoord >> 4, tileEntity.zCoord >> 4)) {
                    Chunk chunk = world.getChunkFromChunkCoords(tileEntity.xCoord >> 4, tileEntity.zCoord >> 4);

                    if (chunk != null) {
                        chunk.cleanChunkBlockTileEntity(tileEntity.xCoord & 15, tileEntity.yCoord, tileEntity.zCoord & 15);
                    }
                }
            }

            if (bIsProfiling) {
                start = end;
                end = System.nanoTime();
                profileAddChunkTime(dim, cx, cz, end - start);
            }
        }
    }

    private boolean isChunkSkippedNow (int dim,int cx,int cz)
    {
        // TODO: punish laggy chunks by skipping / slowing time in them
        return false;
    }

    private void profileAddChunkTime(int dim,int cx,int cz, long time) {
        if (time < 0) time = 0;
        // TODO: sum consumed time per chunk
    }
}
