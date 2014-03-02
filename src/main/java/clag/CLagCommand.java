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
            int mt = parseIntWithMin(sender, par2ArrayOfStr[1], 10);
            CLagTileEntityTicker.instance.timesum_min_slow = mt;
            chatMessage(sender,"clag minslow="+mt);
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
