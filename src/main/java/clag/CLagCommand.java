package clag;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.WorldServer;
import static net.minecraft.util.EnumChatFormatting.*;

public class CLagCommand extends CommandBase {

    public String getCommandName()
    {
        return "clag";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.clag.usage";
    }

    public void chatMessage (ICommandSender sender, String txt)
    {
        FMLLog.info("CLagCommand: chatMessage %s",txt);
        sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(txt).setColor(RED));
    }

    public void processCommand(ICommandSender sender, String[] par2ArrayOfStr)
    {
        if (par2ArrayOfStr.length < 1) return;
        String sub = par2ArrayOfStr[0];
        FMLLog.info("CLagCommand: exec "+sub);
        /*
        int a = parseIntWithMin(par1ICommandSender, par2ArrayOfStr[0], 0);
        */

        EntityPlayerMP p = CLagUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
        WorldServer w = p.getServerForPlayer();

        if (sub.equals("start"))
        {
            CLag.instance.startCLag();
        }
        else if(sub.equals("stop"))
        {
            CLag.instance.stopCLag();
            //CLag.instance.stopCLag(w);
        }
        else if(sub.equals("minslow"))
        {
            int a = parseIntWithMin(sender, par2ArrayOfStr[1], 0);
            int b = parseIntWithMin(sender, par2ArrayOfStr[2], 0);
            int c = parseIntWithMin(sender, par2ArrayOfStr[3], 0);
            CLagTileEntityTicker.instance.timesum_min_slowA = a;
            CLagTileEntityTicker.instance.timesum_min_slowB = b;
            CLagTileEntityTicker.instance.timesum_min_slowC = c;
            chatMessage(sender,"clag minslow="+a+","+b+","+c);
        }
        else if(sub.equals("worst"))
        {
        	CLagTileEntityTicker o = CLagTileEntityTicker.instance;
        	String txt = "";
        	txt += "worst chunk:";
        	txt += " dim="+o.worst_chunk_dim;
        	txt += " cx="+o.worst_chunk_cx;
        	txt += " cz="+o.worst_chunk_cz;
        	txt += " x="+o.worst_chunk_cx*16;
        	txt += " z="+o.worst_chunk_cz*16;
            txt += " time="+o.worst_chunk_time/1000+"mys";
            chatMessage(sender,txt);
        }
        else if(sub.equals("reload"))
        {
        	CLag.instance.loadConfig();
        }
        else if(sub.equals("slow")) // force-slow the chunk the player is currently standing in
        {
            int until_tick = Integer.MAX_VALUE;
            if (par2ArrayOfStr.length >= 2) 
            	until_tick = CLagTileEntityTicker.instance.cur_ticknum + parseIntWithMin(sender, par2ArrayOfStr[1], 0);

            CLagTileEntityTicker.ChunkInfo o = CLagTileEntityTicker.instance.getChunkInfoAtPlayer(p);
            if (o != null) o.force_slow_until_tick = until_tick;
        }
        else
        {
            chatMessage(sender,"clag unknown subcommand");
            chatMessage(sender,"start,stop,minslow [A] [B] [C],worst,reload,slow [NUMTICKS]");
        }
    }

    // compareTo added to make intellij environment happy, not needed on eclipse
    @Override
    public int compareTo(Object o) {
        if (o == null) return 0;
        if (o instanceof ICommand) return this.getCommandName().compareTo(((ICommand)o).getCommandName());
        return 0;
    }
}
