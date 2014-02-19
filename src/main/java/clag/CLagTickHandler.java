package clag;


import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.EnumSet;
import java.util.List;

public class CLagTickHandler implements ITickHandler {
    int iTickNum = 0;

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        ++iTickNum;
        if ((iTickNum % 10) == 0) updateSlowTileEntityList();
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public EnumSet<TickType> ticks() {
        return (EnumSet.of(TickType.SERVER));
    }

    @Override
    public String getLabel() {
        return getClass().getSimpleName();
    }


    public void updateSlowTileEntityList ()
    {
        World world = DimensionManager.getWorld(0); // TODO: iterate over dimensions
        //List tents = (List)CLagUtils.private_get(world,"loadedTileEntityList");
        List tents = world.loadedTileEntityList;
        // tickhandler executes like every 10 ticks.  when it executes it accesses this list, removes all tile-entities in it that are in "slowed" chunks, and stores them it's own reactivation list (weak ref).  10 ticks later it re adds them if they're still "alive" so time doesn't stand completely still.
        // that'd slow down time in laggy chunks by half.   for very laggy chunks we could setup a 2nd list to re-add them like 20 or 40 ticks later

    }
}
