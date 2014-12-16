// clag = chunklag, localize lag to the chunks causing it by slowing down time inside laggy chunks.
package clag;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Mod(modid = CLagInfo.ID, name = CLagInfo.NAME, version = CLagInfo.VERS)
// @NetworkMod(clientSideRequired=false, serverSideRequired=true)
public class CLag {

	// The instance of your mod that Forge uses.
	@Instance(value = CLagInfo.ID)
	public static CLag instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = CLagInfo.CLIENTPROXY, serverSide = CLagInfo.COMMONPROXY)
	public static CommonProxy proxy;

	public File configfile;
	public static boolean debug = true;
	public static boolean autostart = true;
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		CLagUtils.debug("CLag: serverStarting 01");
		CLagUtils.debug("CLag: adding commands...");
		CLagUtils.debug("CLag: serverStarting 02");
		event.registerServerCommand(new CLagCommand());
		event.registerServerCommand(new CLagCommandInfo());
		event.registerServerCommand(new CLagCommandNear());
		CLagUtils.debug("CLag: serverStarting 03");

		// autostart
		if (autostart && !CLagTileEntityTicker.instance.safemode)
		{
			CLagUtils.debug("CLag: autostarting profiling+slowing...");
			startCLag();
		}
	}

	@EventHandler // used in 1.6.2
	//@PreInit    // used in 1.5.2
	public void preInit(FMLPreInitializationEvent event) {
		CLagUtils.debug("CLag: preInit01");
		configfile = event.getSuggestedConfigurationFile();


		CLagUtils.debug("CLag: timesum_min_slowA " + CLagTileEntityTicker.timesum_min_slowA);
		CLagUtils.debug("CLag: timesum_min_slowB " + CLagTileEntityTicker.timesum_min_slowB);
		CLagUtils.debug("CLag: timesum_min_slowC " + CLagTileEntityTicker.timesum_min_slowC);

		loadConfig();
	}

	public void loadConfig() {
		CLagUtils.debug("CLag: loadConfig01");
		// you will be able to find the config file in .minecraft/config/ and it will be named Dummy.cfg
		// here our Configuration has been instantiated, and saved under the name "config"
		Configuration config = new Configuration(configfile);

		// loading the configuration from its file
		config.load();

		FMLLog.info("CLag: loadConfig02");
		
		CLagTileEntityTicker g = CLagTileEntityTicker.instance;

		String cat = Configuration.CATEGORY_GENERAL;
		debug = config.get(cat, "debug", debug).getBoolean(debug);
		autostart = config.get(cat, "autostart", autostart).getBoolean(autostart);
		g.safemode = config.get(cat, "safemode", g.safemode).getBoolean(g.safemode);
		
		CLagTileEntityTicker.profile_interval = config.get(cat, "profile_interval", CLagTileEntityTicker.profile_interval).getInt();
		CLagTileEntityTicker.warn_interval = config.get(cat, "warn_interval", CLagTileEntityTicker.warn_interval).getInt();
		CLagTileEntityTicker.warn_radius = config.get(cat, "warn_radius", CLagTileEntityTicker.warn_radius).getInt();
		CLagTileEntityTicker.max_warn_number_of_players = config.get(cat, "max_warn_number_of_players", CLagTileEntityTicker.max_warn_number_of_players).getInt();
		CLagTileEntityTicker.warn_text = config.get(cat, "warn_text", CLagTileEntityTicker.warn_text).getString();


		CLagTileEntityTicker.timesum_min_slowA = (long) config.get(cat, "timesum_min_slowA", "" + CLagTileEntityTicker.timesum_min_slowA).getDouble(0.0);
		CLagTileEntityTicker.timesum_min_slowB = (long) config.get(cat, "timesum_min_slowB", "" + CLagTileEntityTicker.timesum_min_slowB).getDouble(0.0);
		CLagTileEntityTicker.timesum_min_slowC = (long) config.get(cat, "timesum_min_slowC", "" + CLagTileEntityTicker.timesum_min_slowC).getDouble(0.0);

		CLagTileEntityTicker.slow_down_factorA = config.get(cat, "slow_down_factorA", "" + CLagTileEntityTicker.slow_down_factorA).getInt();
		CLagTileEntityTicker.slow_down_factorB = config.get(cat, "slow_down_factorB", "" + CLagTileEntityTicker.slow_down_factorB).getInt();
		CLagTileEntityTicker.slow_down_factorC = config.get(cat, "slow_down_factorC", "" + CLagTileEntityTicker.slow_down_factorC).getInt();

		String[] arr = config.get(cat, "blacklist", new String[] {}).getStringList();
		g.BlackListClear();
		for (int i=0;i<arr.length;++i) g.BlackListAdd(Block.getBlockFromName(arr[i]));

		CLagUtils.debug("CLag: profile_interval " + CLagTileEntityTicker.profile_interval);
		CLagUtils.debug("CLag: warn_interval " + CLagTileEntityTicker.warn_interval);
		CLagUtils.debug("CLag: warn_radius " + CLagTileEntityTicker.warn_radius);

		CLagUtils.debug("CLag: timesum_min_slowA " + CLagTileEntityTicker.timesum_min_slowA);
		CLagUtils.debug("CLag: timesum_min_slowB " + CLagTileEntityTicker.timesum_min_slowB);
		CLagUtils.debug("CLag: timesum_min_slowC " + CLagTileEntityTicker.timesum_min_slowC);

		CLagUtils.debug("CLag: loadConfig03");

		// saving the configuration to its file
		config.save();
		CLagUtils.debug("CLag: loadConfig04");
	}

	// EventManager eventmanager = new EventManager();

	@EventHandler // used in 1.6.2
	//@Init       // used in 1.5.2
	public void load(FMLInitializationEvent event) {
		CLagUtils.debug("CLag: load 01");
		proxy.registerTickHandler();
		CLagUtils.debug("CLag: load 2");

		MinecraftForge.EVENT_BUS.register(this);
		CLagUtils.debug("CLag: load 3");
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

	/*
	public boolean startProfilingOverworld() {
		World[] arr = new World[1];
		arr[0] = CLagUtils.GetOverworld();
		return startProfiling(Arrays.<World>asList(arr));
	}
	*/
	boolean bIsCLagHookInstalled = false;

	public boolean startCLag() {
		if ( bIsCLagHookInstalled ) return false;
		bIsCLagHookInstalled = true;
		final Collection<World> worlds_ = Arrays.<World>asList(DimensionManager.getWorlds());
		return startCLag(worlds_);
	}

	public boolean stopCLag() {
		if ( !bIsCLagHookInstalled ) return false;
		bIsCLagHookInstalled = false;
		final Collection<World> worlds_ = Arrays.<World>asList(DimensionManager.getWorlds());
		return stopCLag(worlds_);
	}

	// -------------- internal, do not call directly

	// based on https://github.com/nallar/TickProfiler/blob/0449ed2bf76884bcf18847562f8235f3a2e44e9b/src/common/me/nallar/tickprofiler/minecraft/profiling/EntityTickProfiler.java
	public boolean startCLag(final Collection<World> worlds_) {
		CLagUtils.debug("CLag: startCLag for #worlds= " + worlds_.size());
		final Collection<World> worlds = new ArrayList<World>(worlds_);
		synchronized ( CLag.class ) {
			for ( World world_ : worlds ) {
				CLag.instance.installHook(world_);
			}
		}
		return true;
	}


	// based on https://github.com/nallar/TickProfiler/blob/0449ed2bf76884bcf18847562f8235f3a2e44e9b/src/common/me/nallar/tickprofiler/minecraft/profiling/EntityTickProfiler.java
	public boolean stopCLag(final Collection<World> worlds_) {
		CLagUtils.debug("CLag: stopCLag for #worlds= " + worlds_.size());
		final Collection<World> worlds = new ArrayList<World>(worlds_);
		synchronized ( CLag.class ) {
			for ( World world_ : worlds ) {
				CLag.instance.uninstallHook(world_);
			}
		}
		return true;
	}


	// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/TickProfiler.java
	public synchronized void installHook(World world) {
		if ( world.isRemote ) {
			FMLLog.severe("CLag: World " + (world.getClass()) + " seems to be a client world", new Throwable());
		}
		try {
			Field loadedTileEntityField = CLagUtils.getFields(World.class, List.class)[loadedTileEntityFieldIndex];
			new LoadedTileEntityList(world, loadedTileEntityField);
			//Field loadedEntityField = CLagUtils.getFields(World.class, List.class)[loadedEntityFieldIndex];
			//new LoadedEntityList(world, loadedEntityField);
			CLagUtils.debug("CLag: Profiling hooked for world " + (world.getClass()));
		} catch ( Exception e ) {
			FMLLog.severe("CLag: Failed to initialise profiling for world " + (world.getClass()), e);
		}
	}

	// based on https://github.com/nallar/TickProfiler/blob/master/src/common/me/nallar/tickprofiler/minecraft/TickProfiler.java
	public synchronized void uninstallHook(World world) {
		if ( world.isRemote ) {
			FMLLog.severe("CLag: World " + (world.getClass()) + " seems to be a client world", new Throwable());
		}
		try {
			// overrides World.loadedTileEntityList
			Field loadedTileEntityField = CLagUtils.getFields(World.class, List.class)[loadedTileEntityFieldIndex];
			Object loadedTileEntityList = loadedTileEntityField.get(world);
			if ( loadedTileEntityList instanceof EntityList ) {
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
			CLagUtils.debug("CLag: Profiling unhooked for world " + (world.getClass()));
		} catch ( Exception e ) {
			FMLLog.severe("CLag: Failed to unload TickProfiler for world " + (world.getClass()), e);
		}
	}
}
