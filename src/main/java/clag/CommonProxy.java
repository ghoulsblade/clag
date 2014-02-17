package clag;

import cpw.mods.fml.common.FMLLog;

public class CommonProxy {
	// Client stuff
	public void registerRenderers() 
	{
			// Nothing here as the server doesn't render graphics or entities!
	}

	public void registerTickHandler () 
	{
		FMLLog.info("CLag: CommonProxy.registerTickHandler 01");
		//TickRegistry.registerTickHandler(new CLagTickHandlerServer(), Side.SERVER);
		FMLLog.info("CLag: CommonProxy.registerTickHandler 02");
	}
}
