package clag;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {
	public ClientProxy() {
		CLagUtils.debug("CLag.ClientProxy constructor");
	}

	@Override
	public void registerTickHandler() {
		// needed here for "server" part on singleplayer 2x clientproxy, distinguished by Side.SERVER param to TickRegistry
		CLagUtils.debug("CLag: ClientProxy.registerTickHandler 01");
		FMLCommonHandler.instance().bus().register(new CLagTickHandler());
		CLagUtils.debug("CLag: ClientProxy.registerTickHandler 02");
	}
}
