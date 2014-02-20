package clag;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.*;

// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/entitylist/LoadedTileEntityList.java
public class LoadedTileEntityList extends EntityList<TileEntity> {
    public LoadedTileEntityList(World world, Field overriddenField) {
        super(world, overriddenField);
    }

    @Override
    public void tick() {
        // see https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/profiling/EntityTickProfiler.java
        //EntityTickProfiler.ENTITY_TICK_PROFILER.runTileEntities(world, innerList);
        CLagTileEntityTicker.instance.runTileEntities(world, innerList);
    }
}
