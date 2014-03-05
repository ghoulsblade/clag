package clag;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import clag.CLagUtils;

public class CommonProxy {
    public CommonProxy()
    {
	    CLagUtils.debug("CLag.ClientProxy constructor");
    }

	// Client stuff
	public void registerRenderers() 
	{
			// Nothing here as the server doesn't render graphics or entities!
	}

	public void registerTickHandler () 
	{
		CLagUtils.debug("CLag: CommonProxy.registerTickHandler 01");
        TickRegistry.registerTickHandler(new CLagTickHandler(), Side.SERVER);
		CLagUtils.debug("CLag: CommonProxy.registerTickHandler 02");
	}
}
