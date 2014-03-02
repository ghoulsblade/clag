package clag;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {
    public ClientProxy()
    {
        FMLLog.info("CLag.ClientProxy constructor");
    }

	@Override
	public void registerTickHandler () 
	{
		// needed here for "server" part on singleplayer 2x clientproxy, distinguished by Side.SERVER param to TickRegistry
        FMLLog.info("CLag: ClientProxy.registerTickHandler 01");
        TickRegistry.registerTickHandler(new CLagTickHandler(), Side.SERVER);
        FMLLog.info("CLag: ClientProxy.registerTickHandler 02");
	}
}
