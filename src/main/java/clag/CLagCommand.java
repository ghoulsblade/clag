package clag;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

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


    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        if (par2ArrayOfStr.length < 1) return;
        String sub = par2ArrayOfStr[0];
        FMLLog.info("CLagCommand: exec "+sub);
        /*
        int a = parseIntWithMin(par1ICommandSender, par2ArrayOfStr[0], 0);

        */

        EntityPlayerMP p = CLagUtils.getPlayerByCmdSender(par1ICommandSender); // par1ICommandSender.getCommandSenderName()
        WorldServer w = p.getServerForPlayer();

        if (sub == "start")
        {
            CLag.instance.startCLag();
        }
        else if(sub == "stop")
        {
            CLag.instance.stopCLag();
            //CLag.instance.stopCLag(w);
        }
    }


    // compare to added to make intellij environment happy, not needed on eclipse
    // Compares the name of this command to the name of the given command.
    /*
    public int compareTo(ICommand par1ICommand)
    {
        return this.getCommandName().compareTo(par1ICommand.getCommandName());
    }*/
}
