package clag;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.logging.ILogAgent;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.ForgeDummyContainer;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ghoul on 18.02.14.
 */
public class CLagWorld extends WorldServer {
    WorldServer _world;
    public CLagWorld (WorldServer w,
                       MinecraftServer par1MinecraftServer,
                       ISaveHandler par2ISaveHandler,
                       String par3Str,
                       int par4,
                       WorldSettings par5WorldSettings,
                       Profiler par6Profiler,
                       ILogAgent par7ILogAgent)
    {
        super(par1MinecraftServer,
                par2ISaveHandler,
                par3Str,
                par4,
                par5WorldSettings,
                par6Profiler,
                par7ILogAgent
                );
        // public WorldServer(MinecraftServer par1MinecraftServer, ISaveHandler par2ISaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings, Profiler par6Profiler, ILogAgent par7ILogAgent)

        _world = w;
    }


    /**
     * Updates (and cleans up) entities and tile entities
     */
    @Override
    public void updateEntities()
    {
        if (this.playerEntities.isEmpty() && getPersistentChunks().isEmpty())
        {
            int x = get_updateEntityTick();
            set_updateEntityTick(x+1);
            if (x >= 1200)
            {
                return;
            }
        }
        else
        {
            this.resetUpdateEntityTick();
        }

        //super.updateEntities();
        World_updateEntities();
    }



    public int  get_updateEntityTick () { return CLagUtils.private_get_int(this, "updateEntityTick"); }
    public void set_updateEntityTick (int v) {   CLagUtils.private_set_int(this, "updateEntityTick", v); }

    public boolean  get_scanningTileEntities ()          { return CLagUtils.private_get_bool(this, "scanningTileEntities"); }
    public void     set_scanningTileEntities (boolean v) {        CLagUtils.private_set_bool(this, "scanningTileEntities", v); }

    // this method is copied from World to allow modifications to be added
    public void World_updateEntities ()
    {
        this.theProfiler.startSection("entities");
        this.theProfiler.startSection("global");
        int i;
        Entity entity;
        CrashReport crashreport;
        CrashReportCategory crashreportcategory;

        for (i = 0; i < this.weatherEffects.size(); ++i)
        {
            entity = (Entity)this.weatherEffects.get(i);

            try
            {
                ++entity.ticksExisted;
                entity.onUpdate();
            }
            catch (Throwable throwable)
            {
                crashreport = CrashReport.makeCrashReport(throwable, "Ticking entity");
                crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null)
                {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                }
                else
                {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                if (ForgeDummyContainer.removeErroringEntities)
                {
                    FMLLog.severe(crashreport.getCompleteReport());
                    removeEntity(entity);
                }
                else
                {
                    throw new ReportedException(crashreport);
                }
            }

            if (entity.isDead)
            {
                this.weatherEffects.remove(i--);
            }
        }

        this.theProfiler.endStartSection("remove");
        this.loadedEntityList.removeAll(this.unloadedEntityList);
        int j;
        int k;

        for (i = 0; i < this.unloadedEntityList.size(); ++i)
        {
            entity = (Entity)this.unloadedEntityList.get(i);
            j = entity.chunkCoordX;
            k = entity.chunkCoordZ;

            if (entity.addedToChunk && this.chunkExists(j, k))
            {
                this.getChunkFromChunkCoords(j, k).removeEntity(entity);
            }
        }

        for (i = 0; i < this.unloadedEntityList.size(); ++i)
        {
            this.onEntityRemoved((Entity)this.unloadedEntityList.get(i));
        }

        this.unloadedEntityList.clear();
        this.theProfiler.endStartSection("regular");

        for (i = 0; i < this.loadedEntityList.size(); ++i)
        {
            entity = (Entity)this.loadedEntityList.get(i);

            if (entity.ridingEntity != null)
            {
                if (!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity)
                {
                    continue;
                }

                entity.ridingEntity.riddenByEntity = null;
                entity.ridingEntity = null;
            }

            this.theProfiler.startSection("tick");

            if (!entity.isDead)
            {
                try
                {
                    this.updateEntity(entity);
                }
                catch (Throwable throwable1)
                {
                    crashreport = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    crashreportcategory = crashreport.makeCategory("Entity being ticked");
                    entity.addEntityCrashInfo(crashreportcategory);

                    if (ForgeDummyContainer.removeErroringEntities)
                    {
                        FMLLog.severe(crashreport.getCompleteReport());
                        removeEntity(entity);
                    }
                    else
                    {
                        throw new ReportedException(crashreport);
                    }
                }
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("remove");

            if (entity.isDead)
            {
                j = entity.chunkCoordX;
                k = entity.chunkCoordZ;

                if (entity.addedToChunk && this.chunkExists(j, k))
                {
                    this.getChunkFromChunkCoords(j, k).removeEntity(entity);
                }

                this.loadedEntityList.remove(i--);
                this.onEntityRemoved(entity);
            }

            this.theProfiler.endSection();
        }

        this.theProfiler.endStartSection("tileEntities");
        set_scanningTileEntities(true);
        Iterator iterator = this.loadedTileEntityList.iterator();

        while (iterator.hasNext())
        {
            TileEntity tileentity = (TileEntity)iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorldObj() && this.blockExists(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord))
            {
                try
                {
                    tileentity.updateEntity();
                }
                catch (Throwable throwable2)
                {
                    crashreport = CrashReport.makeCrashReport(throwable2, "Ticking tile entity");
                    crashreportcategory = crashreport.makeCategory("Tile entity being ticked");
                    tileentity.func_85027_a(crashreportcategory);
                    if (ForgeDummyContainer.removeErroringTileEntities)
                    {
                        FMLLog.severe(crashreport.getCompleteReport());
                        tileentity.invalidate();
                        setBlockToAir(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord);
                    }
                    else
                    {
                        throw new ReportedException(crashreport);
                    }
                }
            }

            if (tileentity.isInvalid())
            {
                iterator.remove();

                if (this.chunkExists(tileentity.xCoord >> 4, tileentity.zCoord >> 4))
                {
                    Chunk chunk = this.getChunkFromChunkCoords(tileentity.xCoord >> 4, tileentity.zCoord >> 4);

                    if (chunk != null)
                    {
                        chunk.cleanChunkBlockTileEntity(tileentity.xCoord & 15, tileentity.yCoord, tileentity.zCoord & 15);
                    }
                }
            }
        }

        List entityRemoval = (List)CLagUtils.private_get(this, "entityRemoval");
        if (!entityRemoval.isEmpty())
        {
            for (Object tile : entityRemoval)
            {
                ((TileEntity)tile).onChunkUnload();
            }
            this.loadedTileEntityList.removeAll(entityRemoval);
            entityRemoval.clear();
        }

        set_scanningTileEntities(false);

        this.theProfiler.endStartSection("pendingTileEntities");
        List addedTileEntityList = (List)CLagUtils.private_get(this, "addedTileEntityList");

        if (!addedTileEntityList.isEmpty())
        {
            for (int l = 0; l < addedTileEntityList.size(); ++l)
            {
                TileEntity tileentity1 = (TileEntity)addedTileEntityList.get(l);

                if (!tileentity1.isInvalid())
                {
                    if (!this.loadedTileEntityList.contains(tileentity1))
                    {
                        this.loadedTileEntityList.add(tileentity1);
                    }
                }
                else
                {
                    if (this.chunkExists(tileentity1.xCoord >> 4, tileentity1.zCoord >> 4))
                    {
                        Chunk chunk1 = this.getChunkFromChunkCoords(tileentity1.xCoord >> 4, tileentity1.zCoord >> 4);

                        if (chunk1 != null)
                        {
                            chunk1.cleanChunkBlockTileEntity(tileentity1.xCoord & 15, tileentity1.yCoord, tileentity1.zCoord & 15);
                        }
                    }
                }
            }

            addedTileEntityList.clear();
        }

        this.theProfiler.endSection();
        this.theProfiler.endSection();
    }

}
