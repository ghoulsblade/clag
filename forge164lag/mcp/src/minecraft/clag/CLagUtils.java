package clag;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CLagUtils {
    // used by chat-commands
    public static EntityPlayerMP getPlayerByCmdSender (ICommandSender par1ICommandSender)
    {
    	// from CommandHandler
    	String name = par1ICommandSender.getCommandSenderName();
    	EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
		//FMLLog.info("getPlayerByCmdSender name="+name+" found="+((player != null)?"yes":"no"));
    	return player;
    }

}
