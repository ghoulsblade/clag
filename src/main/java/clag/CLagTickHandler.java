package clag;


import cpw.mods.fml.common.FMLLog;
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
        //FMLLog.info("CLag.tickStart " + iTickNum);
        CLagTileEntityTicker.instance.StartTick(iTickNum);
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        CLagTileEntityTicker.instance.EndTick(iTickNum);
    }

    @Override
    public EnumSet<TickType> ticks() {
        return (EnumSet.of(TickType.SERVER));
    }

    @Override
    public String getLabel() {
        return getClass().getSimpleName();
    }
}
