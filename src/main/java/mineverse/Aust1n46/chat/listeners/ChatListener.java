package mineverse.Aust1n46.chat.listeners;

import java.util.HashSet;
import java.util.Set;

import net.essentialsx.api.v2.services.discord.DiscordService;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import com.massivecraft.factions.entity.MPlayer;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.formatting.ChatFormat;
import mineverse.Aust1n46.chat.utilities.Format;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

// This class listens to Paper's AsyncChatEvent and handles VentureChat routing and rendering.
public class ChatListener implements Listener {
	private final boolean essentialsDiscordHook = Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord");
	private final MineverseChat plugin = MineverseChat.getInstance();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAsyncChat(AsyncChatEvent event) {
		MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(event.getPlayer());
		if (mcp == null) {
			return;
		}

		String chat = PlainTextComponentSerializer.plainText().serialize(event.message());
		ChatChannel eventChannel = resolveChannel(chat);
		chat = normalizeChat(chat, eventChannel);

		if (mcp.isEditing()) {
			event.setCancelled(true);
			Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(chat)));
			mcp.setEditing(false);
			return;
		}

		if (handleConversation(event, mcp, chat)) {
			return;
		}

		if (handlePartyChat(event, mcp, chat)) {
			return;
		}

		handlePublicChat(event, mcp, chat, eventChannel);
	}

	private ChatChannel resolveChannel(String chat) {
		ChatChannel eventChannel = ChatChannel.getDefaultChannel();
		for (ChatChannel channel : ChatChannel.getChatChannels()) {
			if (!channel.getQuickSymbol().equals("None") && chat.startsWith(channel.getQuickSymbol())) {
				return channel;
			}
		}
		return eventChannel;
	}

	private String normalizeChat(String chat, ChatChannel eventChannel) {
		String normalized = chat;
		if (!eventChannel.getQuickSymbol().equals("None") && normalized.startsWith(eventChannel.getQuickSymbol())) {
			normalized = normalized.substring(eventChannel.getQuickSymbol().length());
		}
		return normalized.stripLeading();
	}

	private boolean handleConversation(AsyncChatEvent event, MineverseChatPlayer mcp, String chat) {
		if (!mcp.hasConversation()) {
			return false;
		}

		event.setCancelled(true);
		MineverseChatPlayer tp = MineverseChatAPI.getMineverseChatPlayer(mcp.getConversation());
		if (tp == null) {
			mcp.setConversation(null);
			return true;
		}
		if (!tp.isOnline()) {
			Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(ChatColor.RED + tp.getName() + " is not available."));
			if (!mcp.getPlayer().hasPermission("venturechat.spy.override")) {
				for (MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
					if (p.getName().equals(mcp.getName())) {
						continue;
					}
					if (p.isSpy()) {
						Format.sendComponent(p.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(LocalizedMessage.EXIT_PRIVATE_CONVERSATION_SPY.toString()
								.replace("{player_sender}", mcp.getName())
								.replace("{player_receiver}", tp.getName()))));
					}
				}
			}
			mcp.setConversation(null);
			return true;
		}

		if (tp.getIgnores().contains(mcp.getUUID())) {
			Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(LocalizedMessage.IGNORING_MESSAGE.toString()
					.replace("{player}", tp.getName()))));
			return true;
		}
		if (!tp.getMessageToggle()) {
			Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(LocalizedMessage.BLOCKING_MESSAGE.toString()
					.replace("{player}", tp.getName()))));
			return true;
		}

		String filtered = prepareFilteredChat(mcp, chat);
		String send = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcp.getPlayer(), plugin.getConfig().getString("tellformatfrom").replaceAll("sender_", "")));
		String echo = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcp.getPlayer(), plugin.getConfig().getString("tellformatto").replaceAll("sender_", "")));
		String spy = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcp.getPlayer(), plugin.getConfig().getString("tellformatspy").replaceAll("sender_", "")));

		send = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(tp.getPlayer(), send.replaceAll("receiver_", ""))) + filtered;
		echo = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(tp.getPlayer(), echo.replaceAll("receiver_", ""))) + filtered;
		spy = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(tp.getPlayer(), spy.replaceAll("receiver_", ""))) + filtered;

		if (!mcp.getPlayer().hasPermission("venturechat.spy.override")) {
			for (MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
				if (p.getName().equals(mcp.getName()) || p.getName().equals(tp.getName())) {
					continue;
				}
				if (p.isSpy()) {
					Format.sendComponent(p.getPlayer(), Format.legacyToComponent(spy));
				}
			}
		}

		Format.sendComponent(tp.getPlayer(), Format.legacyToComponent(send));
		Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(echo));
		if (tp.hasNotifications()) {
			Format.playMessageSound(tp);
		}
		mcp.setReplyPlayer(tp.getUUID());
		tp.setReplyPlayer(mcp.getUUID());
		if (Database.isEnabled()) {
			Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", "Messaging_Component", chat.replace("'", "''"), "Chat");
		}
		return true;
	}

	private boolean handlePartyChat(AsyncChatEvent event, MineverseChatPlayer mcp, String chat) {
		if (!mcp.isPartyChat()) {
			return false;
		}

		event.setCancelled(true);
		if (!mcp.hasParty()) {
			Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(ChatColor.RED + "You are not in a party."));
			return true;
		}

		String filtered = prepareFilteredChat(mcp, chat);
		String partyformat;
		if (plugin.getConfig().getString("partyformat").equalsIgnoreCase("Default")) {
			partyformat = ChatColor.GREEN + "[" + MineverseChatAPI.getMineverseChatPlayer(mcp.getParty()).getName() + "'s Party] " + mcp.getName() + ":" + filtered;
		} else {
			partyformat = Format.FormatStringAll(plugin.getConfig().getString("partyformat")
					.replace("{host}", MineverseChatAPI.getMineverseChatPlayer(mcp.getParty()).getName())
					.replace("{player}", mcp.getName())) + filtered;
		}

		Component partyComponent = Format.legacyToComponent(partyformat);
		for (MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
			if ((p.hasParty() && p.getParty().toString().equals(mcp.getParty().toString())) || p.isSpy()) {
				Format.sendComponent(p.getPlayer(), partyComponent);
			}
		}
		Format.sendComponent(Bukkit.getConsoleSender(), partyComponent);
		if (Database.isEnabled()) {
			Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", "Party_Component", chat.replace("'", "''"), "Chat");
		}
		return true;
	}

	private void handlePublicChat(AsyncChatEvent event, MineverseChatPlayer mcp, String chat, ChatChannel eventChannel) {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		if (eventChannel.hasSpeakPermission() && !mcp.getPlayer().hasPermission(eventChannel.getSpeakPermission())) {
			event.setCancelled(true);
			Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(LocalizedMessage.CHANNEL_NO_SPEAK_PERMISSIONS.toString())));
			return;
		}

		long dateTimeSeconds = System.currentTimeMillis() / Format.MILLISECONDS_PER_SECOND;
		int chCooldown = eventChannel.hasCooldown() ? eventChannel.getCooldown() : 0;
		try {
			if (mcp.hasCooldown(eventChannel)) {
				long cooldownTime = mcp.getCooldowns().get(eventChannel).longValue();
				if (dateTimeSeconds < cooldownTime) {
					long remainingCooldownTime = cooldownTime - dateTimeSeconds;
					String cooldownString = Format.parseTimeStringFromMillis(remainingCooldownTime * Format.MILLISECONDS_PER_SECOND);
					event.setCancelled(true);
					Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(LocalizedMessage.CHANNEL_COOLDOWN.toString()
							.replace("{cooldown}", cooldownString))));
					return;
				}
			}
			if (eventChannel.hasCooldown() && !mcp.getPlayer().hasPermission("venturechat.cooldown.bypass")) {
				mcp.addCooldown(eventChannel, dateTimeSeconds + chCooldown);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		if (mcp.hasSpam(eventChannel) && plugin.getConfig().getConfigurationSection("antispam").getBoolean("enabled")
				&& !mcp.getPlayer().hasPermission("venturechat.spam.bypass")) {
			long spamcount = mcp.getSpam().get(eventChannel).get(0);
			long spamtime = mcp.getSpam().get(eventChannel).get(1);
			long spamtimeconfig = plugin.getConfig().getConfigurationSection("antispam").getLong("spamnumber");
			long dateTime = System.currentTimeMillis();
			if (dateTimeSeconds < spamtime + plugin.getConfig().getConfigurationSection("antispam").getLong("spamtime")) {
				if (spamcount + 1 >= spamtimeconfig) {
					event.setCancelled(true);
					mcp.getSpam().get(eventChannel).set(0, 0L);
					return;
				} else {
					if (spamtimeconfig % 2 != 0) {
						spamtimeconfig++;
					}
					if (spamcount + 1 == spamtimeconfig / 2) {
						Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(LocalizedMessage.SPAM_WARNING.toString())));
					}
					mcp.getSpam().get(eventChannel).set(0, spamcount + 1);
				}
			} else {
				mcp.getSpam().get(eventChannel).set(0, 1L);
				mcp.getSpam().get(eventChannel).set(1, dateTimeSeconds);
			}
		} else {
			mcp.addSpam(eventChannel);
			mcp.getSpam().get(eventChannel).add(0, 1L);
			mcp.getSpam().get(eventChannel).add(1, dateTimeSeconds);
		}

		double chDistance = eventChannel.hasDistance() ? eventChannel.getDistance() : 0D;
		Set<Player> recipients = new HashSet<>();
		filterRecipients(event, mcp, eventChannel, chDistance, recipients, pluginManager);

		String format = Format.FormatStringAll(eventChannel.getFormat());
		String formattedPrefix = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcp.getPlayer(), format));
		String filteredChat = prepareFilteredChat(mcp, chat);
		if (!Format.startsWithColorCode(filteredChat)) {
			filteredChat = Format.getLastCode(formattedPrefix) + filteredChat;
		}
		ChatFormat chatFormat = ChatFormat.selectChatFormat(mcp.getPlayer(), ChatFormat.getChatFormats());
		if (chatFormat == null) {
			chatFormat = ChatFormat.fallbackFormat("Default");
		}
		Component displayComponent = chatFormat.render(mcp.getPlayer(), format, filteredChat);
		format = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcp.getPlayer(), Format.FormatStringAll(format)));
		String message = Format.stripColor(format + filteredChat);
		int hash = message.hashCode();

		VentureChatEvent ventureChatEvent = new VentureChatEvent(
				mcp,
				mcp.getName(),
				mcp.getNickname(),
				MineverseChat.getVaultPermission().getPrimaryGroup(mcp.getPlayer()),
				eventChannel,
				recipients,
				recipients.size(),
				format,
				filteredChat,
				displayComponent,
				hash);
		Bukkit.getServer().getPluginManager().callEvent(ventureChatEvent);
		event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, renderedMessage) -> displayComponent));

		if (recipients.isEmpty()) {
			String emptyAlert = plugin.getConfig().getString("emptychannelalert", "&6No one is listening to you.");
			if (!emptyAlert.equals("")) {
				Format.sendComponent(mcp.getPlayer(), Format.legacyToComponent(Format.FormatStringAll(emptyAlert)));
			}
		}

		Format.sendComponent(Bukkit.getConsoleSender(), Format.legacyToComponent(ventureChatEvent.getConsoleChat()));
		if (essentialsDiscordHook) {
			Bukkit.getServicesManager().load(DiscordService.class).sendChatMessage(mcp.getPlayer(), filteredChat);
		}
		if (Database.isEnabled()) {
			Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", eventChannel.getName(), filteredChat.replace("'", "''"), "Chat");
		}
		mcp.addMessage(new mineverse.Aust1n46.chat.ChatMessage(displayComponent, message, format + filteredChat, hash));
	}

	private void filterRecipients(AsyncChatEvent event, MineverseChatPlayer mcp, ChatChannel eventChannel, double chDistance,
			Set<Player> recipients, PluginManager pluginManager) {
		Location locsender = mcp.getPlayer().getLocation();
		for (Audience viewer : new HashSet<>(event.viewers())) {
			if (!(viewer instanceof Player target)) {
				continue;
			}
			boolean remove = false;
			if (target != mcp.getPlayer()) {
				MineverseChatPlayer targetMcp = MineverseChatAPI.getOnlineMineverseChatPlayer(target);
				if (targetMcp == null) {
					remove = true;
				}
				if (!remove && plugin.getConfig().getBoolean("ignorechat", false) && targetMcp.getIgnores().contains(mcp.getUUID())) {
					remove = true;
				}
				if (!remove && plugin.getConfig().getBoolean("enable_towny_channel") && pluginManager.isPluginEnabled("Towny")) {
					remove = shouldRemoveForTowny(mcp, target, eventChannel);
				}
				if (!remove && plugin.getConfig().getBoolean("enable_factions_channel") && pluginManager.isPluginEnabled("Factions")) {
					remove = shouldRemoveForFactions(mcp, target, eventChannel);
				}
				if (!remove && chDistance > 0D && !targetMcp.getRangedSpy()) {
					remove = shouldRemoveForDistance(mcp, target, chDistance, locsender);
				}
				if (!remove && !mcp.getPlayer().canSee(target)) {
					remove = true;
				}
			}
			if (remove) {
				event.viewers().remove(target);
			} else {
				recipients.add(target);
			}
		}
	}

	private boolean shouldRemoveForTowny(MineverseChatPlayer mcp, Player target, ChatChannel eventChannel) {
		try {
			TownyUniverse towny = TownyUniverse.getInstance();
			if (eventChannel.getName().equalsIgnoreCase("Town")) {
				Resident r = towny.getResident(target.getName());
				Resident pp = towny.getResident(mcp.getName());
				if (!pp.hasTown() || !r.hasTown()) {
					return true;
				}
				return !r.getTown().getName().equals(pp.getTown().getName());
			}
			if (eventChannel.getName().equalsIgnoreCase("Nation")) {
				Resident r = towny.getResident(target.getName());
				Resident pp = towny.getResident(mcp.getName());
				if (!pp.hasNation() || !r.hasNation()) {
					return true;
				}
				return !r.getTown().getNation().getName().equals(pp.getTown().getNation().getName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private boolean shouldRemoveForFactions(MineverseChatPlayer mcp, Player target, ChatChannel eventChannel) {
		try {
			if (eventChannel.getName().equalsIgnoreCase("Faction")) {
				MPlayer mplayer = MPlayer.get(mcp.getPlayer());
				MPlayer mplayerp = MPlayer.get(target);
				if (!mplayer.hasFaction() || !mplayerp.hasFaction()) {
					return true;
				}
				return !mplayer.getFactionName().equals(mplayerp.getFactionName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private boolean shouldRemoveForDistance(MineverseChatPlayer mcp, Player target, double chDistance, Location locsender) {
		Location locreceip = target.getLocation();
		if (locreceip.getWorld() != mcp.getPlayer().getWorld()) {
			return true;
		}
		Location diff = locreceip.subtract(locsender);
		return Math.abs(diff.getX()) > chDistance || Math.abs(diff.getZ()) > chDistance || Math.abs(diff.getY()) > chDistance;
	}

	private String prepareFilteredChat(MineverseChatPlayer mcp, String chat) {
		String filtered = chat;
		if (mcp.hasFilter()) {
			filtered = Format.FilterChat(filtered);
		}
		if (mcp.getPlayer().hasPermission("venturechat.color.legacy")) {
			filtered = Format.FormatStringLegacyColor(filtered);
		}
		if (mcp.getPlayer().hasPermission("venturechat.color")) {
			filtered = Format.FormatStringColor(filtered);
		}
		if (mcp.getPlayer().hasPermission("venturechat.format")) {
			filtered = Format.FormatString(filtered);
		}
		return " " + filtered;
	}
}
