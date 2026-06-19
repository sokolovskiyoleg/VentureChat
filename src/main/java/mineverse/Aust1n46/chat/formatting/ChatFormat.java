package mineverse.Aust1n46.chat.formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.ClickAction;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.utilities.Format;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class ChatFormat {
	private static final String PERMISSION_PREFIX = "venturechat.chat.";
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^\\{\\}]+)\\}");

	private static List<ChatFormat> chatFormats;

	private final String name;
	private final String permission;
	private final int priority;
	private final List<ChatFormatAttribute> attributes;

	public ChatFormat(String name, String permission, int priority, List<ChatFormatAttribute> attributes) {
		this.name = name;
		this.permission = permission;
		this.priority = priority;
		this.attributes = attributes;
	}

	public static void initialize() {
		chatFormats = new ArrayList<ChatFormat>();
		ConfigurationSection chatFormatSection = MineverseChat.getInstance().getConfig().getConfigurationSection("chatformatting");
		if (chatFormatSection == null) {
			return;
		}
		for (String chatFormat : chatFormatSection.getKeys(false)) {
			int priority = chatFormatSection.getInt(chatFormat + ".priority", 0);
			String permissionText = chatFormatSection.getString(chatFormat + ".permissions", "");
			String permission = normalizePermission(permissionText);
			if (permission == null && !chatFormat.equalsIgnoreCase("Default")) {
				permission = PERMISSION_PREFIX + chatFormat;
			}
			List<ChatFormatAttribute> attributes = readAttributes(chatFormatSection.getConfigurationSection(chatFormat + ".attributes"));
			chatFormats.add(new ChatFormat(chatFormat, permission, priority, attributes));
		}
	}

	private static List<ChatFormatAttribute> readAttributes(ConfigurationSection attributeSection) {
		List<ChatFormatAttribute> attributes = new ArrayList<ChatFormatAttribute>();
		if (attributeSection == null) {
			return attributes;
		}
		for (String attribute : attributeSection.getKeys(false)) {
			List<String> hoverText = attributeSection.getStringList(attribute + ".hover_text");
			String clickActionText = attributeSection.getString(attribute + ".click_action", "none");
			ClickAction clickAction;
			try {
				clickAction = ClickAction.valueOf(clickActionText.toUpperCase());
			} catch (IllegalArgumentException | NullPointerException exception) {
				MineverseChat.getInstance().getServer().getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Illegal click_action: " + clickActionText));
				continue;
			}
			String clickText = attributeSection.getString(attribute + ".click_text", "");
			attributes.add(new ChatFormatAttribute(attribute, hoverText, clickAction, clickText));
		}
		return attributes;
	}

	public static Collection<ChatFormat> getChatFormats() {
		return chatFormats == null ? List.of() : chatFormats;
	}

	public static ChatFormat getChatFormat(String name) {
		if (name == null || chatFormats == null) {
			return null;
		}
		for (ChatFormat chatFormat : chatFormats) {
			if (chatFormat.getName().equalsIgnoreCase(name)) {
				return chatFormat;
			}
		}
		return null;
	}

	public static ChatFormat selectChatFormat(Player player, Collection<ChatFormat> formats) {
		if (formats == null || formats.isEmpty()) {
			return null;
		}
		ChatFormat selected = null;
		ChatFormat defaultFormat = null;
		for (ChatFormat format : formats) {
			if (format.permission == null) {
				if (defaultFormat == null || format.getPriority() < defaultFormat.getPriority()) {
					defaultFormat = format;
				}
				continue;
			}
			if (format.matches(player)) {
				if (selected == null || format.getPriority() < selected.getPriority()) {
					selected = format;
				}
			}
		}
		return selected != null ? selected : defaultFormat;
	}

	public static ChatFormat fallbackFormat(String name) {
		return new ChatFormat(name, null, Integer.MAX_VALUE, List.of());
	}

	private static String normalizePermission(String permissionText) {
		if (permissionText == null || permissionText.isBlank() || permissionText.equalsIgnoreCase("none")) {
			return null;
		}
		if (permissionText.startsWith(PERMISSION_PREFIX)) {
			return permissionText;
		}
		return PERMISSION_PREFIX + permissionText;
	}

	private boolean matches(Player player) {
		if (player == null) {
			return false;
		}
		return this.permission != null && player.hasPermission(this.permission);
	}

	public Component render(Player player, String format, String renderedChatLegacy) {
		if (format == null) {
			format = "";
		}
		if (renderedChatLegacy == null) {
			renderedChatLegacy = "";
		}
		Component prefix = renderTemplate(player, format);
		if (renderedChatLegacy.isBlank()) {
			return prefix;
		}
		return prefix.append(Format.legacyToComponentWithUrls(renderedChatLegacy));
	}

	private Component renderTemplate(Player player, String format) {
		if (format == null || format.isBlank()) {
			return Component.empty();
		}
		Component rendered = Component.empty();
		String template = format;
		int lastIndex = 0;
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
		while (matcher.find()) {
			String literal = template.substring(lastIndex, matcher.start());
			if (!literal.isEmpty()) {
				rendered = rendered.append(Format.legacyToComponentWithUrls(Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, literal))));
			}

			String placeholder = template.substring(matcher.start(), matcher.end());
			String placeholderKey = matcher.group(1);
			String expanded = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, placeholder));
			Component placeholderComponent = Format.legacyToComponentWithUrls(expanded);
			ChatFormatAttribute attribute = findAttribute(placeholderKey);
			if (attribute != null) {
				placeholderComponent = applyAttribute(player, placeholderComponent, attribute);
			}
			rendered = rendered.append(placeholderComponent);
			lastIndex = matcher.end();
		}

		String tail = template.substring(lastIndex);
		if (!tail.isEmpty()) {
			rendered = rendered.append(Format.legacyToComponentWithUrls(Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, tail))));
		}

		return rendered;
	}

	private ChatFormatAttribute findAttribute(String placeholderKey) {
		for (ChatFormatAttribute attribute : this.attributes) {
			if (attribute.getName().equalsIgnoreCase(placeholderKey)) {
				return attribute;
			}
		}
		return null;
	}

	private Component applyAttribute(Player player, Component component, ChatFormatAttribute attribute) {
		Component result = component;
		if (!attribute.getHoverText().isEmpty()) {
			Component hoverComponent = Component.empty();
			boolean first = true;
			for (String line : attribute.getHoverText()) {
				if (!first) {
					hoverComponent = hoverComponent.append(Component.text("\n"));
				}
				String renderedLine = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, line));
				hoverComponent = hoverComponent.append(Format.legacyToComponentWithUrls(renderedLine));
				first = false;
			}
			result = result.hoverEvent(HoverEvent.showText(hoverComponent));
		}
		if (attribute.getClickAction() != ClickAction.NONE) {
			String clickText = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(player, attribute.getClickText()));
			switch (attribute.getClickAction()) {
			case SUGGEST_COMMAND:
				result = result.clickEvent(ClickEvent.suggestCommand(clickText));
				break;
			case RUN_COMMAND:
				result = result.clickEvent(ClickEvent.runCommand(clickText));
				break;
			case OPEN_URL:
				result = result.clickEvent(ClickEvent.openUrl(clickText));
				break;
			default:
				break;
			}
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public String getPermission() {
		return permission;
	}

	public int getPriority() {
		return priority;
	}

	public List<ChatFormatAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return "ChatFormat{name='" + name + "', permission='" + permission + "', priority=" + priority + "}";
	}
}
