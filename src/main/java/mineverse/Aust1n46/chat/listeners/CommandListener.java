package mineverse.Aust1n46.chat.listeners;

import java.io.FileNotFoundException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.alias.Alias;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;

public class CommandListener implements Listener {
	private MineverseChat plugin = MineverseChat.getInstance();

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws FileNotFoundException {
		if (event.getPlayer() == null) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Event.getPlayer() returned null in PlayerCommandPreprocessEvent"));
			return;
		}
		ConfigurationSection cs = plugin.getConfig().getConfigurationSection("commandspy");
		Boolean wec = cs.getBoolean("worldeditcommands", true);
		MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(event.getPlayer());
		if (!mcp.getPlayer().hasPermission("venturechat.commandspy.override")) {
			for (MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
				if (p.hasCommandSpy()) {
					if (wec) {
						p.getPlayer().sendMessage(Format.FormatStringAll(cs.getString("format").replace("{player}", mcp.getName()).replace("{command}", event.getMessage())));
					} else {
						if (!(event.getMessage().toLowerCase().startsWith("//"))) {
							p.getPlayer().sendMessage(Format.FormatStringAll(cs.getString("format").replace("{player}", mcp.getName()).replace("{command}", event.getMessage())));
						} else {
							if (!(event.getMessage().toLowerCase().startsWith("//"))) {
								p.getPlayer().sendMessage(ChatColor.GOLD + mcp.getName() + ": " + event.getMessage());
							}
						}
					}
				}
			}
		}

		String message = event.getMessage();

		if (Database.isEnabled()) {
			Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", "Command_Component", event.getMessage().replace("'", "''"), "Command");
		}

		for (Alias a : Alias.all()) {
			String cmdName = message.toLowerCase().substring(1).split(" ")[0];
			String aliasName = a.getName().toLowerCase();
			String shortLabel = cmdName.contains(":") ? cmdName.substring(cmdName.lastIndexOf(':') + 1) : cmdName;
			if (!shortLabel.equals(aliasName)) {
				continue;
			}
			int cmdEnd = cmdName.length() + 1;
			for (String s : a.getComponents()) {
				if (!mcp.getPlayer().hasPermission(a.getPermission()) && a.hasPermission()) {
					mcp.getPlayer().sendMessage(LocalizedMessage.ALIAS_NO_PERMISSION.toString());
					event.setCancelled(true);
					return;
				}
				int num = 1;
				if (message.length() < cmdEnd + 1 || a.getArguments() == 0)
					num = 0;
				int arg = 0;
				if (message.substring(cmdEnd + num).length() == 0)
					arg = 1;
				String[] args = message.substring(cmdEnd + num).split(" ");
				String send = "";
				if (args.length - arg < a.getArguments()) {
					String keyword = "arguments.";
					if (a.getArguments() == 1)
						keyword = "argument.";
					mcp.getPlayer().sendMessage(ChatColor.RED + "Invalid arguments for this alias, enter at least " + a.getArguments() + " " + keyword);
					event.setCancelled(true);
					return;
				}
				for (int b = 0; b < args.length; b++) {
					send += " " + args[b];
				}
				if (send.length() > 0)
					send = send.substring(1);
				if (s.startsWith("Json:")) {
					String json = s.substring(5).replace("$", send);
					BaseComponent[] components = ComponentSerializer.parse(json);
					mcp.getPlayer().spigot().sendMessage(components);
					event.setCancelled(true);
					continue;
				}
				s = Format.FormatStringAll(s);
				if (mcp.getPlayer().hasPermission("venturechat.color.legacy")) {
					send = Format.FormatStringLegacyColor(send);
				}
				if (mcp.getPlayer().hasPermission("venturechat.color")) {
					send = Format.FormatStringColor(send);
				}
				if (mcp.getPlayer().hasPermission("venturechat.format")) {
					send = Format.FormatString(send);
				}
				if (s.startsWith("Command:")) {
					mcp.getPlayer().chat(s.substring(9).replace("$", send));
					event.setCancelled(true);
				}
				if (s.startsWith("Message:")) {
					mcp.getPlayer().sendMessage(s.substring(9).replace("$", send));
					event.setCancelled(true);
				}
				if (s.startsWith("Broadcast:")) {
					Format.broadcastToServer(s.substring(11).replace("$", send));
					event.setCancelled(true);
				}
			}
		}
	}

	// old 1.8 command map
	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		if (Database.isEnabled()) {
			Database.writeVentureChat("N/A", "Console", "Local", "Command_Component", event.getCommand().replace("'", "''"), "Command");
		}
	}

}
