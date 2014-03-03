// regularly (not every tick) measure cpu time consumed by chunks. if a chunk is causing lag, slow down time inside that chunk : skip ticks

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/profiling/EntityTickProfiler.java
public class CLagTileEntityTicker {
    public static CLagTileEntityTicker instance = new CLagTileEntityTicker();
    boolean bIsProfiling = false;
    public final int PROFILE_INTERVAL = 10*20; // every x*20 seconds
    public int cur_ticknum = 0;

    public final long TIMESUM_MICRO = 1000*1; // System.nanoTime -> nano*1000 = micro, micro*1000 = milli
    public final long TIMESUM_MILLI = 1000*TIMESUM_MICRO;
    public final long TIMESUM_SECOND = 1000*TIMESUM_MILLI;
    public final long TIMESUM_TICK = TIMESUM_SECOND/20;
    public long timesum_min_slow = TIMESUM_TICK/20;
    // public long timesum_min_slow = 10;

    public int slow_down_factor = 20;

    public CLagTileEntityTicker ()
    {

    }

    public void StartTick (int iTickNum) {
        cur_ticknum = iTickNum;
        bIsProfiling = (iTickNum % PROFILE_INTERVAL) == 0;
        if (bIsProfiling) FMLLog.info("CLagTileEntityTicker:StartTick profile "+iTickNum);

        lastchunk_dim = Integer.MAX_VALUE; // make sure cur_ticknum check (sum reset) is done for last used chunk as well
        if (bIsProfiling) worst_chunk_time = 0;
        ChunkSkipStartTick();
    }
    public void EndTick (int iTickNum) {
        if (bIsProfiling) FMLLog.info("CLagTileEntityTicker:EndTick profile "+iTickNum+",worst_chunk_time="+worst_chunk_time);
    }

    public void runTileEntities(World world, ArrayList<TileEntity> toTick) {
        IChunkProvider chunkProvider = world.getChunkProvider();
        Iterator<TileEntity> iterator = toTick.iterator();
        long end = System.nanoTime();
        long start;
        int dim = world.provider.dimensionId;


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
                if (end > start) profileAddChunkTime(dim, cx, cz, x, tileEntity.yCoord, z, end - start);
            }
        }
    }

    // ---------- ChunkInfo

    static public class ChunkInfoPos
    {
        public int dim;
        public int cx;
        public int cz;
        public ChunkInfoPos (int _dim,int _cx,int _cz)
        {
            dim = _dim;
            cx = _cx;
            cz = _cz;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            ChunkInfoPos o = (ChunkInfoPos) obj;
            return  dim == o.dim && cx == o.cx && cz == o.cz;
        }

        @Override
        public int hashCode() { return dim + 10*cx + 1000*cz; }
    }

    static public class ChunkInfo
    {
        public int ticknum;
        public int iTimeSum;
        public long worst_time;
        public int worst_x;
        public int worst_y;
        public int worst_z;
        public ChunkInfo ()
        {
            ticknum = 0;
            iTimeSum = 0;
            worst_time = 0;
        }
    }

    public Map<ChunkInfoPos, ChunkInfo> mChunkInfo = new HashMap<ChunkInfoPos, ChunkInfo>();

    /// get or create
    public ChunkInfo getChunkInfo (int dim,int cx,int cz)
    {
        ChunkInfoPos coord = new ChunkInfoPos(dim,cx,cz);
        ChunkInfo o = mChunkInfo.get(coord);
        if (o == null)
        {
            o = new ChunkInfo();
            mChunkInfo.put(coord, o);
        }
        return o;
    }

    // ---------- internals


    private int lastskip_dim = Integer.MAX_VALUE;
    private int lastskip_cx = Integer.MAX_VALUE;
    private int lastskip_cz = Integer.MAX_VALUE;
    private boolean lastskip_v = false;

    private void ChunkSkipStartTick ()
    {
        // TODO: update for current tick

        // reset cache
        lastskip_dim = Integer.MAX_VALUE;
    }


    private boolean calcIsChunkSkippedNow (int dim,int cx,int cz)
    {
        if ((cur_ticknum % slow_down_factor) == 0) return false;
        ChunkInfo o = getChunkInfo(dim,cx,cz);
        return o.iTimeSum > timesum_min_slow;
    }

    /// punish laggy chunks by skipping / slowing time in them
    private boolean isChunkSkippedNow (int dim,int cx,int cz)
    {
        if (lastskip_dim == dim && lastskip_cx == cx && lastskip_cz == cz) return lastskip_v;
        lastskip_dim = dim;
        lastskip_cx = cx;
        lastskip_cz = cz;
        lastskip_v = calcIsChunkSkippedNow(dim,cx,cz);
        return lastskip_v;
    }

    private int lastchunk_dim = Integer.MAX_VALUE;
    private int lastchunk_cx = Integer.MAX_VALUE;
    private int lastchunk_cz = Integer.MAX_VALUE;
    private ChunkInfo lastchunkinfo = null;
    public long worst_chunk_time = 0;

    // sum consumed time per chunk
    private void profileAddChunkTime(int dim,int cx,int cz, int x, int y, int z, long dt) {
        if (lastchunk_dim != dim || lastchunk_cx != cx || lastchunk_cz != cz)
        {
            if (lastchunkinfo != null &&
                worst_chunk_time < lastchunkinfo.iTimeSum)
                worst_chunk_time = lastchunkinfo.iTimeSum;
            lastchunk_dim = dim;
            lastchunk_cx = cx;
            lastchunk_cz = cz;
            lastchunkinfo = getChunkInfo(dim,cx,cz);

            // reset sum on tick start
            if (lastchunkinfo.ticknum != cur_ticknum)
            {
                lastchunkinfo.ticknum = cur_ticknum;
                lastchunkinfo.iTimeSum = 0;
                lastchunkinfo.worst_time = 0;
            }
        }
        lastchunkinfo.iTimeSum += dt;

        // collect worst tileentity to inform nearby players and help them fix the issue
        if (dt > lastchunkinfo.worst_time)
        {
            lastchunkinfo.worst_time = dt;
            lastchunkinfo.worst_x = x;
            lastchunkinfo.worst_y = y;
            lastchunkinfo.worst_z = z;
        }
    }
}
