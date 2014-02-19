// clag = chunklag, localize lag to the chunks causing it by slowing down time inside laggy chunks.
package clag;

import net.minecraft.command.ServerCommandManager;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

@Mod(modid=CLagInfo.ID, name=CLagInfo.NAME, version=CLagInfo.VERS)
// @NetworkMod(clientSideRequired=false, serverSideRequired=true)
public class CLag {

        // The instance of your mod that Forge uses.
        @Instance(value = CLagInfo.ID)
        public static CLag instance;
        
        // Says where the client and server 'proxy' code is loaded.
        @SidedProxy(clientSide=CLagInfo.CLIENTPROXY, serverSide=CLagInfo.COMMONPROXY)
        public static CommonProxy proxy;
        
        @EventHandler
        public void serverStarting(FMLServerStartingEvent event){
        	FMLLog.info("CLag: serverStarting 01");
        	ServerCommandManager scm = (ServerCommandManager)event.getServer().getCommandManager();
        	FMLLog.info("CLag: adding commands...");
        	FMLLog.info("CLag: serverStarting 02");
			/*
        	scm.registerCommand(new CLagCommandTest());
			*/

        	FMLLog.info("CLag: serverStarting 03");
        }
        
        @EventHandler // used in 1.6.2
        //@PreInit    // used in 1.5.2
        public void preInit(FMLPreInitializationEvent event) {
            FMLLog.info("CLag: preInit01");
			// you will be able to find the config file in .minecraft/config/ and it will be named Dummy.cfg
			// here our Configuration has been instantiated, and saved under the name "config"
			//Configuration config = new Configuration(event.getSuggestedConfigurationFile());
			
            // loading the configuration from its file
            //config.load();

            FMLLog.info("CLag: preInit02");
            // saving the configuration to its file
            //config.save();
            FMLLog.info("CLag: preInit05");
        }

    	// EventManager eventmanager = new EventManager();
    	
        @EventHandler // used in 1.6.2
        //@Init       // used in 1.5.2
        public void load(FMLInitializationEvent event) {
            FMLLog.info("CLag: load 01");
            proxy.registerTickHandler();
            FMLLog.info("CLag: load 2");

            MinecraftForge.EVENT_BUS.register(this);
            FMLLog.info("CLag: load 3");
        }
       
        @EventHandler // used in 1.6.2
        //@PostInit   // used in 1.5.2
        public void postInit(FMLPostInitializationEvent event) {
                // Stub Method
        }

        /*
        @EventHandler // used in 1.6.2
        // @ForgeSubscribe
        public void onEntityCanUpdate(EntityEvent.CanUpdate event) {
            // TODO: check if in sleeping chunk and disable
            // event.canUpdate = false;
        }
        */

}
