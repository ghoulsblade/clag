package clag;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
 * Created by ghoul on 02.03.14.
 */
public class CLagCommandInfo extends CommandBase {

	@Override
    public String getCommandName()
    {
        return "clag-info";
    }

	@Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.clag-info.usage";
    }

	@Override
    public void processCommand(ICommandSender sender, String[] par2ArrayOfStr)
    {
        CLagUtils.debug("CLagCommandInfo");

        EntityPlayerMP p = CLagUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
        WorldServer w = p.getServerForPlayer();

        CLagTileEntityTicker.ChunkInfo o = CLagTileEntityTicker.instance.getChunkInfoAtPlayer(p);
        long wt = CLagTileEntityTicker.instance.worst_chunk_time;
        long mt = CLagTileEntityTicker.instance.timesum_min_slowA;

        String txt = "clag-info: ";
        txt += " slow="+mt/1000+"mys";
        //txt += " worst-chunk="+wt/1000+"µs";
        long percent = 0;
        if (wt > 0) percent = (100*o.iTimeSum)/wt;
        txt += " this="+o.iTimeSum/1000+"mys ("+percent+"% of worst)";
        txt += " "+o.worst_x+","+o.worst_y+","+o.worst_z;
        int f = CLagTileEntityTicker.instance.getSlowFactor(o);
        if (f > 1) txt += " !!!SLOWED(x"+f+")!!! ";

        CLagUtils.chatMessage(sender, txt);
    }

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
