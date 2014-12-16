package clag;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.util.EnumSet;

public class CLagTickHandler {
	int iTickNum = 0;

	@SubscribeEvent
	public void tickServer(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			++iTickNum;
			//FMLLog.info("CLag.tickStart " + iTickNum);
			CLagTileEntityTicker.instance.StartTick(iTickNum);
		} else {
			CLagTileEntityTicker.instance.EndTick(iTickNum);
		}
	}
}
