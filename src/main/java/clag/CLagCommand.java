package clag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.apache.commons.io.FileUtils;

public class CLagCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "clag";
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "commands.clag.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] par2ArrayOfStr) {
		if ( par2ArrayOfStr.length < 1 ) return;
		String sub = par2ArrayOfStr[0];
		CLagUtils.debug("CLagCommand: exec " + sub);
	    /*
        int a = parseIntWithMin(par1ICommandSender, par2ArrayOfStr[0], 0);
        */

		EntityPlayerMP p = CLagUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
		WorldServer w = p.getServerForPlayer();

		CLagTileEntityTicker g = CLagTileEntityTicker.instance;

		if ( sub.equals("start") ) {
			CLag.instance.startCLag();
		} else if ( sub.equals("stop") ) {
			CLag.instance.stopCLag();
			//CLag.instance.stopCLag(w);
		} else if ( sub.equals("minslow") ) {
			int a = parseIntWithMin(sender, par2ArrayOfStr[1], 0);
			int b = parseIntWithMin(sender, par2ArrayOfStr[2], 0);
			int c = parseIntWithMin(sender, par2ArrayOfStr[3], 0);
			CLagTileEntityTicker.instance.timesum_min_slowA = a;
			CLagTileEntityTicker.instance.timesum_min_slowB = b;
			CLagTileEntityTicker.instance.timesum_min_slowC = c;
			CLagUtils.chatMessage(sender, "clag minslow=" + a + "," + b + "," + c);
		} else if ( sub.equals("worst") ) {
			// if (!CLag.instance.bIsCLagHookInstalled) { CLagUtils.chatMessage(sender, "clag is not running (no hook)"); return; }
			// if (!g.bTickOverride) { CLagUtils.chatMessage(sender, "clag is not running (no tick-override)"); return; }
			
			String txt = "";

			CLagTileEntityTicker.ChunkInfo o = g.worst_chunk_info;
			if (o == null)
			{
				txt += "(no chunk profiled)";
			} else 
			{
				txt += "worst chunk: ";
				txt += " " + o.worst_x + "," + o.worst_y + "," + o.worst_z;
				txt += " time=" + g.worst_chunk_time / 1000 + "mys";
			}
			CLagUtils.chatMessage(sender, txt);
		} else if ( sub.equals("reload") ) {
			CLag.instance.loadConfig();
		} else if ( sub.equals("slow") ) // force-slow the chunk the player is currently standing in
		{
			int until_tick = Integer.MAX_VALUE;
			if ( par2ArrayOfStr.length >= 2 )
				until_tick = CLagTileEntityTicker.instance.cur_ticknum + parseIntWithMin(sender, par2ArrayOfStr[1], 0);

			CLagTileEntityTicker.ChunkInfo o = CLagTileEntityTicker.instance.getChunkInfoAtPlayer(p);
			if ( o != null ) 
			{
				o.force_slow_until_tick = until_tick;
				CLagUtils.chatMessage(sender, "chunk slowed: "+(o.pos.cx*16)+","+(o.pos.cz*16));
			}
			
		} else if ( sub.equals("warn") ) // reset warning interval
		{
			CLagTileEntityTicker.instance.last_warn_tick = Integer.MIN_VALUE;
			CLagUtils.chatMessage(sender, "resetting warning interval");

		// --------- debug functions
		} else if ( sub.equals("enable_tick") ) // debug: 0 to disable the custom tick code entirely
		{
			g.bTickOverride = parseIntWithMin(sender, par2ArrayOfStr[1], 0) != 0;
			CLagUtils.chatMessage(sender, String.format("bTickOverride=%d",g.bTickOverride ? 1 : 0));

		// --------- debug functions
		} else if ( sub.equals("enable_slow") ) // debug: 0 to disable the tick-skipping entirely
		{
			g.bEnableSlowing = parseIntWithMin(sender, par2ArrayOfStr[1], 0) != 0;
			CLagUtils.chatMessage(sender, String.format("bEnableSlowing=%d",g.bEnableSlowing ? 1 : 0));

		// --------- debug functions
		} else if ( sub.equals("force_vanilla") ) // debug: use vanilla tick code without any if-checks
		{
			g.bForceVanillaTick = parseIntWithMin(sender, par2ArrayOfStr[1], 0) != 0;
			CLagUtils.chatMessage(sender, String.format("bForceVanillaTick=%d",g.bForceVanillaTick ? 1 : 0));

		} else if ( sub.equals("profile_only") ) // 1= warnings only, no slowing possible, the hook is installed only during a profile tick
		{
			g.safemode = parseIntWithMin(sender, par2ArrayOfStr[1], 0) != 0;
			if (!g.safemode) CLag.instance.startCLag();
			if (g.safemode) CLag.instance.stopCLag();
			CLagUtils.chatMessage(sender, String.format("safemode=%d",g.safemode ? 1 : 0));
			
		} else if ( sub.equals("debug") ) // output infos
		{
			CLagUtils.chatMessage(sender, String.format("bIsCLagHookInstalled=%d",CLag.instance.bIsCLagHookInstalled ? 1 : 0));
			CLagUtils.chatMessage(sender, String.format("bTickOverride=%d",g.bTickOverride ? 1 : 0));
			CLagUtils.chatMessage(sender, String.format("bEnableSlowing=%d",g.bEnableSlowing ? 1 : 0));
			CLagUtils.chatMessage(sender, String.format("safemode=%d",g.safemode ? 1 : 0));
			CLagUtils.chatMessage(sender, String.format("bForceVanillaTick=%d",g.bForceVanillaTick ? 1 : 0));
			
			int t = g.last_exc_type;
			String tn = (t <= 0 || Block.blocksList[t] == null) ? "null" : Block.blocksList[t].getUnlocalizedName();
			if (g.c_exc == 0) 
					CLagUtils.chatMessage(sender, String.format("exceptions #%d ",g.c_exc));
			else	CLagUtils.chatMessage(sender, String.format("exceptions #%d last: dim=%d %d,%d,%d type=%d=%s",g.c_exc,g.last_exc_dim,g.last_exc_x,g.last_exc_y,g.last_exc_z,t,tn));

			long dt = System.nanoTime() - g.last_profile_tick_start;
			CLagUtils.chatMessage(sender, String.format("last profile %d secs ago, dur=%d mys",dt/1000/1000/1000,g.last_profile_tick_duration/1000));

			
			
		} else if ( sub.equals("blacklist") )
		{
			if (par2ArrayOfStr.length >= 2)
			{
				int t = parseIntWithMin(sender, par2ArrayOfStr[1], 0);
				g.BlackListAdd(t);
			} else
			{
				g.BlackListClear();
			}
			
		} else if ( sub.equals("reset") ) // reset some vars
		{
			g.c_exc = 0;

		} else if ( sub.equals("pdump") ) // dump player coords
		{
			int dim=0; // overworld
			World world = DimensionManager.getWorld(dim);
			if ( world == null ) return;
			
	    	ArrayList lines = new ArrayList(world.playerEntities.size());
			Iterator iterator = world.playerEntities.iterator();
			while ( iterator.hasNext() ) {
				EntityPlayerMP o = (EntityPlayerMP) iterator.next();
				lines.add(String.format("player,%d,%d,%d,%d,\"%s\"\n",dim,(int)o.posX,(int)o.posY,(int)o.posZ,o.getEntityName()));
			}

			try {
				String path = "clag-pdump.txt";
				File file = new File(DimensionManager.getCurrentSaveRootDirectory(), path);
				FileUtils.writeLines(file, lines);
				CLagUtils.chatMessage(sender, "clag pdump: saved as "+file.getAbsolutePath());
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
				CLagUtils.chatMessage(sender, "clag pdump: error saving file");
	        }
		} else {
			CLagUtils.chatMessage(sender, "clag unknown subcommand");
			CLagUtils.chatMessage(sender, "start,stop,minslow [A] [B] [C],worst,reload,slow [NUMTICKS],warn,enable_tick [0/1],enable_slow [0/1],force_vanilla [0/1],profile_only [0/1],debug,blacklist [id],reset,pdump");
		}
	}

	// compareTo added to make intellij environment happy, not needed on eclipse
	@Override
	public int compareTo(Object o) {
		return 0;
	}

	// ------------------- permissions
	// based on https://github.com/nallar/TickProfiler/blob/f88aa64e1db0c944aa65a48339beb709d1cbf896/src/common/me/nallar/tickprofiler/minecraft/commands/Command.java
	
	/*
	protected boolean requireOp() {
		return false;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
		Boolean permission = null;
		if (commandSender instanceof EntityPlayer) {
			permission = checkPermission((EntityPlayer) commandSender);
		}
		if (permission != null) return permission;
		return !requireOp() || super.canCommandSenderUseCommand(commandSender);
	}

	private static IPermissions permissions;
	
	public Boolean checkPermission(EntityPlayer entityPlayer) {
		if (permissions == null) return null;
		return permissions.has(entityPlayer, this.getClass().getName());
	}

	public static void checkForPermissions() {
		for (ModContainer modContainer : Loader.instance().getActiveModList()) {
			Object mod = modContainer.getMod(); 
			if (mod instanceof IPermissions) {
				Command.permissions = (IPermissions) mod;
				Log.info("Using " + Log.toString(mod) + ':' + modContainer.getModId() + " as a permissions source.");
				return;
			}
		}
	}
	*/
}
