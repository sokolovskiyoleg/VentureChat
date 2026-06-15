package mineverse.Aust1n46.chat;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.comphenix.protocol.ProtocolLibrary;

import mineverse.Aust1n46.chat.alias.Alias;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.channel.ChatChannelInfo;
import mineverse.Aust1n46.chat.command.VentureCommandExecutor;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.database.PlayerData;
import mineverse.Aust1n46.chat.gui.GuiSlot;
import mineverse.Aust1n46.chat.json.JsonFormat;
import mineverse.Aust1n46.chat.listeners.ChatListener;
import mineverse.Aust1n46.chat.listeners.CommandListener;
import mineverse.Aust1n46.chat.listeners.LoginListener;
import mineverse.Aust1n46.chat.listeners.PacketListenerLegacyChat;
import mineverse.Aust1n46.chat.localization.Localization;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

/**
 * VentureChat Minecraft plugin for servers running Spigot or Paper software.
 *
 * @author Aust1n46
 */
public class MineverseChat extends JavaPlugin {
	// Event constants
	public static final boolean ASYNC = true;
	public static final boolean SYNC = false;
	
	public static final int LINE_LENGTH = 40;
	
	// DiscordSRV backwards compatibility
	@Deprecated
	public static ChatChannelInfo ccInfo;

	@Deprecated
	public static Set<MineverseChatPlayer> players = new HashSet<MineverseChatPlayer>();
	@Deprecated
	public static Set<MineverseChatPlayer> onlinePlayers = new HashSet<MineverseChatPlayer>();

	// Vault
	private static Permission permission = null;
	private static Chat chat = null;

	@Override
	public void onEnable() {
		ccInfo = new ChatChannelInfo();
		
		try {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Initializing..."));
			if(!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			if(!file.exists()) {
				Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Config not found! Generating file."));
				saveDefaultConfig();
			}
			else {
				Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Config found! Loading file."));
			}
			saveResource("example_config_always_up_to_date!.yml", true);
		}
		catch(Exception ex) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - &cCould not load configuration! Something unexpected went wrong!"));
		}
		
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Checking for Vault..."));
		
		if(!setupPermissions() || !setupChat()) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - &cCould not find Vault and/or a Vault compatible permissions plugin!"));
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		initializeConfigReaders();
		
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Loading player data"));
		PlayerData.loadLegacyPlayerData();
		PlayerData.loadPlayerData();
		
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			Database.initializeMySQL();
		});

		VentureCommandExecutor.initialize();

		registerListeners();
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Registering Listeners"));
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Attaching to Executors"));

		PluginManager pluginManager = getServer().getPluginManager();
		if(pluginManager.isPluginEnabled("Towny")) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling Towny Formatting"));
		}
		if(pluginManager.isPluginEnabled("Jobs")) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling Jobs Formatting"));
		}
		if(pluginManager.isPluginEnabled("Factions")) {
			String version = pluginManager.getPlugin("Factions").getDescription().getVersion();
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling Factions Formatting version " + version));
		}
		if(pluginManager.isPluginEnabled("PlaceholderAPI")) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling PlaceholderAPI Hook"));
		}
		
		new VentureChatPlaceholders().register();
		
		startRepeatingTasks();
		
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabled Successfully"));	
	}
	
	@Override
	public void onDisable() {
		PlayerData.savePlayerData();
		MineverseChatAPI.clearMineverseChatPlayerMap();
		MineverseChatAPI.clearNameMap();
		MineverseChatAPI.clearOnlineMineverseChatPlayerMap();
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Disabling..."));
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Disabled Successfully"));
	}
	
	private void startRepeatingTasks() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.runTaskTimerAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				PlayerData.savePlayerData();
				if(getConfig().getString("loglevel", "info").equals("debug")) {
					Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Saving Player Data"));
				}
			}
		}, 0L, getConfig().getInt("saveinterval") * 1200); //one minute * save interval
	}
	
	private void registerListeners() {
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(new LoginListener(), this);
		if (VersionHandler.isUnder_1_19()) {
			ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListenerLegacyChat());
		}
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if(permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return(permission != null);
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if(chatProvider != null) {
			chat = chatProvider.getProvider();
		}
		return(chat != null);
	}
	
	public static MineverseChat getInstance() {
		return getPlugin(MineverseChat.class);	
	}
	
	public static void initializeConfigReaders() {
		Localization.initialize();
		Alias.initialize();
		JsonFormat.initialize();
		GuiSlot.initialize();
		ChatChannel.initialize();
	}
	
	public static Chat getVaultChat() {
		return chat;
	}
	
	public static Permission getVaultPermission() {
		return permission;
	}

}
