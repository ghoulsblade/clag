package clag;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;

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

    // private field access

    public static Object private_get (Object o,String name,Object oDefault)
    {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(o);
        } catch (Exception e) {}
        return oDefault;
    }

    public static void private_set (Object o,String name,Object v)
    {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(o,v);
        } catch (Exception e) {}
    }

    public static Object private_get (Object o,String name) { return private_get(o,name,null); }

    public static int private_get_int (Object o,String name) { return (Integer)private_get(o,name,(Integer)0); }
    public static void private_set_int (Object o,String name,int v) { private_set(o,name,(Integer)v); }

    public static boolean private_get_bool (Object o,String name) { return (Boolean)private_get(o,name,(Boolean)false); }
    public static void private_set_bool (Object o,String name,boolean v) { private_set(o,name,(Boolean)v); }
}
