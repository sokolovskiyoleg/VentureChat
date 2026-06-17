package mineverse.Aust1n46.chat.command.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import org.bukkit.entity.Player;

public class Broadcast extends Command {
	private MineverseChat plugin = MineverseChat.getInstance();

	public Broadcast() {
		super("broadcast");
	}

	@Override
	public boolean execute(CommandSender sender, String command, String[] args) {
		ConfigurationSection bs = plugin.getConfig().getConfigurationSection("broadcast");
		if(bs == null) return false;
		String broadcastPermissions = bs.getString("permissions", "None");

		if (!broadcastPermissions.equalsIgnoreCase("None") && !sender.hasPermission(broadcastPermissions)) {
			sender.sendMessage(LocalizedMessage.COMMAND_NO_PERMISSION.toString());
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(LocalizedMessage.COMMAND_INVALID_ARGUMENTS
					.toString()
					.replace("{command}", "/broadcast")
					.replace("{args}", "[msg]"));
			return true;
		}

		String format;
		if(sender instanceof Player){
			format = bs.getString("player_format", "&8[&cBroadcast %player_name%&8] {message}");
		} else {
			format = bs.getString("console_format", "&8[&cCONSOLE&8] {message}");
		}

		String bc = String.join(" ", args);
		String message = Format.processPlaceHolders(sender, format);
		message = Format.FormatStringAll(message);
		message = message.replace("{message}", bc);

		Format.broadcastToServer(sender, message);
		return true;
	}
}
