package clag;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {
	// Client stuff
	public void registerRenderers() 
	{
			// Nothing here as the server doesn't render graphics or entities!
	}

	public void registerTickHandler () 
	{
		FMLLog.info("CLag: CommonProxy.registerTickHandler 01");
        TickRegistry.registerTickHandler(new CLagTickHandler(), Side.SERVER);
		FMLLog.info("CLag: CommonProxy.registerTickHandler 02");
	}
}
