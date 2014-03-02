package clag;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
 * Created by ghoul on 02.03.14.
 */
public class CLagCommandInfo extends CLagCommand {
    public String getCommandName()
    {
        return "clag-info";
    }

    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.clag-info.usage";
    }

    public void processCommand(ICommandSender sender, String[] par2ArrayOfStr)
    {
        FMLLog.info("CLagCommandInfo");

        EntityPlayerMP p = CLagUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
        WorldServer w = p.getServerForPlayer();

        int dim = p.dimension;
        int cx = ((int)p.posX) >> 4;
        int cz = ((int)p.posZ) >> 4;
        CLagTileEntityTicker.ChunkInfo o = CLagTileEntityTicker.instance.getChunkInfo(dim,cx,cz);
        long wt = CLagTileEntityTicker.instance.worst_chunk_time;
        long mt = CLagTileEntityTicker.instance.timesum_min_slow;

        String txt = "clag-info: ";
        txt += " slow="+mt/1000+"µs";
        //txt += " worst-chunk="+wt/1000+"µs";
        long percent = 0;
        if (wt > 0) percent = (100*o.iTimeSum)/wt;
        txt += " this="+o.iTimeSum/1000+"µs ("+percent+"% of worst)";
        txt += " "+o.worst_x+","+o.worst_y+","+o.worst_z;
        if (o.iTimeSum > mt) txt += " !!!SLOWED(x"+CLagTileEntityTicker.instance.slow_down_factor+")!!! ";

        chatMessage(sender,txt);
    }
}
