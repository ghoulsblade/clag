package clag;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
 * Created by ghoul on 02.03.14.
 */
public class CLagCommandNear extends CommandBase {

	@Override
	public String getCommandName() {
		return "clag-near";
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "commands.clag-near.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] par2ArrayOfStr) {
		CLagUtils.debug("CLagCommandNear");

		EntityPlayerMP p = CLagUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
		WorldServer w = p.getServerForPlayer();

		String txt = "nearby laggy chunks:\n";
		int r = CLagTileEntityTicker.instance.warn_radius / 16;
		for (int ax=-r;ax<=r;++ax)
		for (int az=-r;az<=r;++az)
		{
			CLagTileEntityTicker.ChunkInfo o = CLagTileEntityTicker.instance.getChunkInfoAtPlayer(p,ax*16,az*16);
			int f = CLagTileEntityTicker.instance.getSlowFactor(o);
			if ( f <= 1 ) continue;
			{
				txt += " " + o.iTimeSum / 1000 + "mys : ";
				if (o.worst_time > 0)
						txt += o.worst_x + "," + o.worst_y + "," + o.worst_z;
				else	txt += (o.pos.cx*16+8) + "," + 256 + "," + (o.pos.cz*16+8);
				txt += " !!!SLOWED(x" + f + ")!!! \n";
			}
		}
		CLagUtils.chatMessage(sender, txt);
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
