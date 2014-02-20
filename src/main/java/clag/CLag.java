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
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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




    // based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/TickProfiler.java
    private static final int loadedEntityFieldIndex = 0;
    private static final int loadedTileEntityFieldIndex = 2;

    public boolean startProfilingOverworld() {
        World[] arr = new World[1];
        arr[0] = CLagUtils.GetOverworld();
        return startProfiling(Arrays.<World>asList(arr));
    }

    public boolean startProfiling() {
        final Collection<World> worlds_ = Arrays.<World>asList(DimensionManager.getWorlds());
        return startProfiling(worlds_);
    }

    // based on https://github.com/nallar/TickProfiler/blob/0449ed2bf76884bcf18847562f8235f3a2e44e9b/src/common/me/nallar/tickprofiler/minecraft/profiling/EntityTickProfiler.java
    public boolean startProfiling(final Collection<World> worlds_) {
        final Collection<World> worlds = new ArrayList<World>(worlds_);
        synchronized (CLag.class) {
            for (World world_ : worlds) {
                CLag.instance.hookProfiler(world_);
            }
        }
        return true;
    }


    // based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/TickProfiler.java
    public synchronized void hookProfiler(World world) {
        if (world.isRemote) {
            FMLLog.severe("CLag: World " + (world.getClass()) + " seems to be a client world", new Throwable());
        }
        try {
            Field loadedTileEntityField = CLagUtils.getFields(World.class, List.class)[loadedTileEntityFieldIndex];
            new LoadedTileEntityList(world, loadedTileEntityField);
            //Field loadedEntityField = CLagUtils.getFields(World.class, List.class)[loadedEntityFieldIndex];
            //new LoadedEntityList(world, loadedEntityField);
            FMLLog.finer("CLag: Profiling hooked for world " + (world.getClass()));
        } catch (Exception e) {
            FMLLog.severe("CLag: Failed to initialise profiling for world " + (world.getClass()), e);
        }
    }

    // based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/TickProfiler.java
    public synchronized void unhookProfiler(World world) {
        if (world.isRemote) {
            FMLLog.severe("CLag: World " + (world.getClass()) + " seems to be a client world", new Throwable());
        }
        try {
            // overrides World.loadedTileEntityList
            Field loadedTileEntityField = CLagUtils.getFields(World.class, List.class)[loadedTileEntityFieldIndex];
            Object loadedTileEntityList = loadedTileEntityField.get(world);
            if (loadedTileEntityList instanceof EntityList) {
                ((EntityList) loadedTileEntityList).unhook();
            } else {
                FMLLog.severe("CLag: Looks like another mod broke CLag's replacement tile entity list in world: " + (world.getClass()));
            }
            /*
            Field loadedEntityField = CLagUtils.getFields(World.class, List.class)[loadedEntityFieldIndex];
            Object loadedEntityList = loadedEntityField.get(world);
            if (loadedEntityList instanceof EntityList) {
                ((EntityList) loadedEntityList).unhook();
            } else {
                FMLLog.severe("CLag: Looks like another mod broke TickProfiler's replacement entity list in world: " + (world.getClass()));
            }*/
            FMLLog.finer("CLag: Profiling unhooked for world " + (world.getClass()));
        } catch (Exception e) {
            FMLLog.severe("CLag: Failed to unload TickProfiler for world " + (world.getClass()), e);
        }
    }
}
