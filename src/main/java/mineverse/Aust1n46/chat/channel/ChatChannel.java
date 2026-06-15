package mineverse.Aust1n46.chat.channel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.utilities.Format;

/**
 * Chat channel object pojo. Class also contains static initialization methods
 * for reading chat channels from the config file.
 * 
 * @author Aust1n46
 */
public class ChatChannel {
	private static final String PERMISSION_PREFIX = "venturechat.";
	private static final String NO_PERMISSIONS = "venturechat.none";

	private static MineverseChat plugin = MineverseChat.getInstance();
	private static ChatChannel defaultChatChannel;
	private static HashMap<String, ChatChannel> chatChannels;

	private String name;
	private String permission;
	private String speakPermission;
	private String quickSymbol;
	private double distance;
	private boolean filter;
	private String format;
	private int cooldown;
	private String prefix;

	/**
	 * Read chat channels from config file and initialize channel array.
	 */
	public static void initialize() {
		chatChannels = new HashMap<String, ChatChannel>();
		ConfigurationSection cs = plugin.getConfig().getConfigurationSection("channels");
		for (String key : cs.getKeys(false)) {
			String name = key;
			String permission = cs.getString(key + ".permissions", "None");
			String speakPermission = cs.getString(key + ".speak_permissions", "None");
			boolean filter = cs.getBoolean(key + ".filter", true);
			String format = cs.getString(key + ".format", "Default");
			String quickSymbol = cs.getString(key + ".quickSymbol", "None");
			double distance = cs.getDouble(key + ".distance", (double) 0);
			int cooldown = cs.getInt(key + ".cooldown", 0);
			boolean autojoin = cs.getBoolean(key + ".autojoin", false);
			ChatChannel chatChannel = new ChatChannel(name, permission, speakPermission,
					filter, quickSymbol, distance, cooldown, format);
			chatChannels.put(name.toLowerCase(), chatChannel);
		}
		// Error handling for missing default channel in the config.
		if(chatChannels.isEmpty()) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - &cNo channels found, created default local channel!"));
			defaultChatChannel = new ChatChannel("Local",  "None", "None", true,
					"None", 150, 0, "&7[L]&r {player_displayname}&c:");
			chatChannels.put("Local", defaultChatChannel);
		}
		defaultChatChannel = ChatChannel.getChannel("Local");
	}

	/**
	 * Parameterized constructor a {@link ChatChannel}.
	 *
	 * @param name
	 * @param permission
	 * @param speakPermission
	 * @param filter
	 * @param distance
	 * @param cooldown
	 * @param format
	 */
	public ChatChannel(String name, String permission, String speakPermission, boolean filter,
					   String quickSymbol, double distance, int cooldown, String format) {
		this.name = name;
		this.permission = PERMISSION_PREFIX + permission;
		this.speakPermission = PERMISSION_PREFIX + speakPermission;
		this.filter = filter;
		this.quickSymbol = quickSymbol;
		this.distance = distance;
		this.cooldown = cooldown;
		this.format = format;
	}

	/**
	 * Get list of chat channels.
	 * 
	 * @return {@link Collection}&lt{@link ChatChannel}&gt
	 */
	public static Collection<ChatChannel> getChatChannels() {
		return new HashSet<ChatChannel>(chatChannels.values());
	}

	/**
	 * Get a chat channel by name.
	 * 
	 * @param channelName
	 *            name of channel to get.
	 * @return {@link ChatChannel}
	 */
	public static ChatChannel getChannel(String channelName) {
		return chatChannels.get(channelName.toLowerCase());
	}

	/**
	 * Checks if the chat channel exists.
	 * 
	 * @param channelName
	 *            name of channel to check.
	 * @return true if channel exists, false otherwise.
	 */
	public static boolean isChannel(String channelName) {
		return getChannel(channelName) != null;
	}

	/**
	 * Check if the chat channel is BungeeCord enabled.
	 *
	 * @return {@link Boolean#TRUE} if the chat channel is BungeeCord enabled,
	 *         {@link Boolean#FALSE} otherwise.
	 */
	public Boolean getBungee() {
		return false;
	}

	/**
	 * Get default chat channel.
	 * 
	 * @return {@link ChatChannel}
	 */
	public static ChatChannel getDefaultChannel() {
		return defaultChatChannel;
	}

	/**
	 * Get the name of the chat channel.
	 * 
	 * @return {@link String}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the format of the chat channel.
	 * 
	 * @return {@link String}
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Get the cooldown of the chat channel in seconds.
	 * 
	 * @return int
	 */
	public int getCooldown() {
		return cooldown;
	}
	
	/**
	 * Get the prefix of the chat channel.
	 * @return String
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Get the permissions node for the chat channel.
	 * 
	 * @return {@link String}
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * Get quickSymbol of chat channel
	 */
	public String getQuickSymbol() { return quickSymbol; }

	/**
	 * Get the distance of the chat channel in blocks.
	 * 
	 * @return {@link Double}
	 */
	public Double getDistance() {
		return Double.valueOf(distance);
	}

	/**
	 * Checks if the chat channel has a distance set.
	 * 
	 * @return {@link Boolean#TRUE} if the distance is greater than zero,
	 *         {@link Boolean#FALSE} otherwise.
	 */
	public Boolean hasDistance() {
		return Boolean.valueOf(distance > 0);
	}

	/**
	 * Checks if the chat channel has a cooldown set.
	 * 
	 * @return {@link Boolean#TRUE} if the cooldown is greater than zero,
	 *         {@link Boolean#FALSE} otherwise.
	 */
	public Boolean hasCooldown() {
		return Boolean.valueOf(cooldown > 0);
	}

	/**
	 * Checks if the chat channel has a permission set.
	 * 
	 * @return {@link Boolean#TRUE} if the permission does not equal
	 *         {@link ChatChannel#NO_PERMISSIONS}, {@link Boolean#FALSE} otherwise.
	 */
	public Boolean hasPermission() {
		return Boolean.valueOf(!permission.equalsIgnoreCase(NO_PERMISSIONS));
	}

	/**
	 * Checks if the chat channel has a speak permission set.
	 * 
	 * @return true if the speak permission does not equal
	 *         {@link ChatChannel#NO_PERMISSIONS}, false otherwise.
	 */
	public boolean hasSpeakPermission() {
		return !speakPermission.equalsIgnoreCase(NO_PERMISSIONS);
	}

	/**
	 * Get the speak permissions node for the chat channel.
	 * 
	 * @return {@link String}
	 */
	public String getSpeakPermission() {
		return speakPermission;
	}

	/**
	 * Checks if the chat channel has the filter enabled.
	 * 
	 * @return {@link Boolean#TRUE} if the chat channel has the filter enabled,
	 *         {@link Boolean#FALSE} otherwise.
	 */
	public Boolean isFiltered() {
		return Boolean.valueOf(filter);
	}

	/**
	 * Compares the chat channel by name to determine equality.
	 * 
	 * @param channel
	 *            Object to compare for equality.
	 * @return true if the objects are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object channel) {
		return channel instanceof ChatChannel && this.name.equals(((ChatChannel) channel).getName());
	}

}
