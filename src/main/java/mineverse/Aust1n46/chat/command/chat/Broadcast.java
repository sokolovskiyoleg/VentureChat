package mineverse.Aust1n46.chat.command.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
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
		String broadcastColor = bs.getString("color", "white");
		String broadcastPermissions = bs.getString("permissions", "None");
		String rawBroadcastDisplayTag = bs.getString("displaytag", "[Broadcast]");
		if (broadcastPermissions.equalsIgnoreCase("None") || sender.hasPermission(broadcastPermissions)) {
			if (args.length > 0) {
				String bc = String.join("", args);
				bc = Format.FormatStringAll(bc);

				// Хардколд обработки плейсхолдеров =)
				String broadcastDisplayTag;
				if(sender instanceof Player player){
					broadcastDisplayTag = PlaceholderAPI.setBracketPlaceholders(player, rawBroadcastDisplayTag);
				} else {
					broadcastDisplayTag = rawBroadcastDisplayTag.replace("%player_name%", "Console").
							replace("{player_name}", "Console");
				}
				broadcastDisplayTag = Format.FormatStringAll(broadcastDisplayTag);

				Format.broadcastToServer(broadcastDisplayTag + ChatColor.valueOf(broadcastColor.toUpperCase()) + " " + bc);
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
