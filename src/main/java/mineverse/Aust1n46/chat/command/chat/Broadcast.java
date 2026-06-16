package mineverse.Aust1n46.chat.command.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;

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
		if (broadcastPermissions.equalsIgnoreCase("None") || sender.hasPermission(broadcastPermissions)) {
			if (args.length > 0) {
				String format = bs.getString("format", "&8[&cBroadcast&8] {message}");
				String bc = String.join(" ", args);
				String message = Format.FormatStringAll(Format.processPlaceHolders(sender, format).replace("{message}", bc));
				Format.broadcastToServer(sender, message);
				return true;
			} else {
				sender.sendMessage(LocalizedMessage.COMMAND_INVALID_ARGUMENTS.toString().replace("{command}", "/broadcast").replace("{args}", "[msg]"));
				return true;
			}
		} else {
			sender.sendMessage(LocalizedMessage.COMMAND_NO_PERMISSION.toString());
			return true;
		}
	}
}
