package mineverse.Aust1n46.chat.utilities;

import static mineverse.Aust1n46.chat.MineverseChat.getInstance;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.NonNull;

/**
 * Class containing chat formatting methods.
 */
public class Format {
	public static final int LEGACY_COLOR_CODE_LENGTH = 2;
	public static final int HEX_COLOR_CODE_LENGTH = 14;
	public static final String HEX_COLOR_CODE_PREFIX = "#";
	public static final char BUKKIT_COLOR_CODE_PREFIX_CHAR = '\u00A7';
	public static final String BUKKIT_COLOR_CODE_PREFIX = String.valueOf(BUKKIT_COLOR_CODE_PREFIX_CHAR);
	public static final String BUKKIT_HEX_COLOR_CODE_PREFIX = "x";
	public static final String DEFAULT_COLOR_CODE = BUKKIT_COLOR_CODE_PREFIX + "f";

	private static final Pattern LEGACY_CHAT_COLOR_DIGITS_PATTERN = Pattern.compile("&([0-9])");
	private static final Pattern LEGACY_CHAT_COLOR_PATTERN = Pattern.compile(
			"(?<!(&x(&[a-fA-F0-9]){5}))(?<!(&x(&[a-fA-F0-9]){4}))(?<!(&x(&[a-fA-F0-9]){3}))(?<!(&x(&[a-fA-F0-9]){2}))(?<!(&x(&[a-fA-F0-9]){1}))(?<!(&x))(&)([0-9a-fA-F])");
	private static final Pattern LEADING_COLOR_CODE_PATTERN = Pattern.compile("^\\s*" + Pattern.quote(BUKKIT_COLOR_CODE_PREFIX)
			+ "(?:x(?:" + Pattern.quote(BUKKIT_COLOR_CODE_PREFIX) + "[0-9a-fA-F]){6}|[0-9a-fA-FrR])");
	
	private static final Pattern PLACEHOLDERAPI_PLACEHOLDER_PATTERN = Pattern.compile("\\{([^\\{\\}]+)\\}");
	private static final Pattern URL_PATTERN = Pattern.compile("(?i)\\b((?:https?://)?(?:www\\.)?[a-z0-9][a-z0-9.-]+\\.[a-z]{2,}(?:/[\\w\\-./?%&=+#~:]*)?)");
	
	public static final long MILLISECONDS_PER_DAY = 86400000;
	public static final long MILLISECONDS_PER_HOUR = 3600000;
	public static final long MILLISECONDS_PER_MINUTE = 60000;
	public static final long MILLISECONDS_PER_SECOND = 1000;
	
	public static final String DEFAULT_MESSAGE_SOUND = "minecraft:entity.player.levelup";
	public static final String DEFAULT_LEGACY_MESSAGE_SOUND = "LEVEL_UP";

	public static Component legacyToComponent(String legacyText) {
		if (legacyText == null || legacyText.isBlank()) {
			return Component.empty();
		}
		return LegacyComponentSerializer.legacySection().deserialize(legacyText);
	}

	public static Component legacyToComponentWithUrls(String legacyText) {
		if (legacyText == null || legacyText.isBlank()) {
			return Component.empty();
		}
		List<LegacySegment> segments = splitLegacySegments(legacyText);
		Component result = Component.empty();
		boolean underlineUrls = getInstance().getConfig().getBoolean("underlineurls", true);
		for (LegacySegment segment : segments) {
			result = result.append(renderLegacySegment(segment, underlineUrls));
		}
		return result;
	}

	private static Component renderLegacySegment(LegacySegment segment, boolean underlineUrls) {
		Component result = Component.empty();
		Matcher matcher = URL_PATTERN.matcher(segment.text());
		int lastIndex = 0;
		while (matcher.find()) {
			String before = segment.text().substring(lastIndex, matcher.start());
			if (!before.isEmpty()) {
				result = result.append(Component.text(before).style(segment.style()));
			}
			String url = matcher.group(1);
			String clickTarget = normalizeUrl(url);
			Style urlStyle = underlineUrls
					? segment.style().decorate(TextDecoration.UNDERLINED)
					: segment.style();
			result = result.append(Component.text(url)
					.style(urlStyle)
					.clickEvent(ClickEvent.openUrl(clickTarget))
					.hoverEvent(HoverEvent.showText(Component.text(clickTarget))));
			lastIndex = matcher.end();
		}
		String tail = segment.text().substring(lastIndex);
		if (!tail.isEmpty()) {
			result = result.append(Component.text(tail).style(segment.style()));
		}
		return result;
	}

	private static List<LegacySegment> splitLegacySegments(String legacyText) {
		List<LegacySegment> segments = new ArrayList<LegacySegment>();
		StringBuilder buffer = new StringBuilder();
		LegacyStyle style = new LegacyStyle();
		for (int i = 0; i < legacyText.length();) {
			char current = legacyText.charAt(i);
			if (current == BUKKIT_COLOR_CODE_PREFIX_CHAR && i + 1 < legacyText.length()) {
				if (buffer.length() > 0) {
					segments.add(new LegacySegment(buffer.toString(), style.toStyle()));
					buffer.setLength(0);
				}
				char code = legacyText.charAt(i + 1);
				if ((code == 'x' || code == 'X') && isLegacyHexCode(legacyText, i)) {
					style.applyHex(legacyText.substring(i + 2, i + 14));
					i += 14;
					continue;
				}
				if (style.apply(code)) {
					i += 2;
					continue;
				}
				buffer.append(current).append(code);
				i += 2;
			} else {
				buffer.append(current);
				i++;
			}
		}
		if (buffer.length() > 0) {
			segments.add(new LegacySegment(buffer.toString(), style.toStyle()));
		}
		return segments;
	}

	private static boolean isLegacyHexCode(String legacyText, int index) {
		if (index + 13 >= legacyText.length()) {
			return false;
		}
		for (int offset = 0; offset < 6; offset++) {
			int sectionIndex = index + 2 + (offset * 2);
			if (legacyText.charAt(sectionIndex) != BUKKIT_COLOR_CODE_PREFIX_CHAR) {
				return false;
			}
			char digit = legacyText.charAt(sectionIndex + 1);
			if (Character.digit(digit, 16) == -1) {
				return false;
			}
		}
		return true;
	}

	private static String normalizeUrl(String url) {
		if (url == null || url.isBlank()) {
			return "";
		}
		if (url.regionMatches(true, 0, "http://", 0, 7) || url.regionMatches(true, 0, "https://", 0, 8)) {
			return url;
		}
		return "http://" + url;
	}

	public static String componentToPlainText(Component component) {
		if (component == null) {
			return "";
		}
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	public static void sendComponent(CommandSender sender, Component component) {
		if (sender == null || component == null) {
			return;
		}
		sender.sendMessage(component);
	}

	private static final class LegacySegment {
		private final String text;
		private final Style style;

		private LegacySegment(String text, Style style) {
			this.text = text;
			this.style = style;
		}

		private String text() {
			return text;
		}

		private Style style() {
			return style;
		}
	}

	private static final class LegacyStyle {
		private TextColor color = NamedTextColor.WHITE;
		private boolean bold;
		private boolean obfuscated;
		private boolean italic;
		private boolean strikethrough;
		private boolean underlined;

		private boolean apply(char code) {
			switch (Character.toLowerCase(code)) {
			case '0':
				setColor(NamedTextColor.BLACK);
				return true;
			case '1':
				setColor(NamedTextColor.DARK_BLUE);
				return true;
			case '2':
				setColor(NamedTextColor.DARK_GREEN);
				return true;
			case '3':
				setColor(NamedTextColor.DARK_AQUA);
				return true;
			case '4':
				setColor(NamedTextColor.DARK_RED);
				return true;
			case '5':
				setColor(NamedTextColor.DARK_PURPLE);
				return true;
			case '6':
				setColor(NamedTextColor.GOLD);
				return true;
			case '7':
				setColor(NamedTextColor.GRAY);
				return true;
			case '8':
				setColor(NamedTextColor.DARK_GRAY);
				return true;
			case '9':
				setColor(NamedTextColor.BLUE);
				return true;
			case 'a':
				setColor(NamedTextColor.GREEN);
				return true;
			case 'b':
				setColor(NamedTextColor.AQUA);
				return true;
			case 'c':
				setColor(NamedTextColor.RED);
				return true;
			case 'd':
				setColor(NamedTextColor.LIGHT_PURPLE);
				return true;
			case 'e':
				setColor(NamedTextColor.YELLOW);
				return true;
			case 'f':
				setColor(NamedTextColor.WHITE);
				return true;
			case 'k':
				this.obfuscated = true;
				return true;
			case 'l':
				this.bold = true;
				return true;
			case 'm':
				this.strikethrough = true;
				return true;
			case 'n':
				this.underlined = true;
				return true;
			case 'o':
				this.italic = true;
				return true;
			case 'r':
				resetDecorations();
				setColor(NamedTextColor.WHITE);
				return true;
			default:
				return false;
			}
		}

		private void applyHex(String hexDigits) {
			if (hexDigits == null || hexDigits.length() != 12) {
				return;
			}
			StringBuilder hex = new StringBuilder("#");
			for (int i = 0; i < hexDigits.length(); i += 2) {
				hex.append(hexDigits.charAt(i + 1));
			}
			try {
				TextColor textColor = TextColor.fromHexString(hex.toString());
				if (textColor != null) {
					setColor(textColor);
				}
			} catch (IllegalArgumentException exception) {
				// Ignore invalid hex codes and continue with the previous style.
			}
		}

		private void setColor(TextColor color) {
			this.color = color;
			resetDecorations();
		}

		private void resetDecorations() {
			this.bold = false;
			this.obfuscated = false;
			this.italic = false;
			this.strikethrough = false;
			this.underlined = false;
		}

		private Style toStyle() {
			Style style = Style.style().color(this.color).build();
			if (this.bold) {
				style = style.decorate(TextDecoration.BOLD);
			}
			if (this.obfuscated) {
				style = style.decorate(TextDecoration.OBFUSCATED);
			}
			if (this.italic) {
				style = style.decorate(TextDecoration.ITALIC);
			}
			if (this.strikethrough) {
				style = style.decorate(TextDecoration.STRIKETHROUGH);
			}
			if (this.underlined) {
				style = style.decorate(TextDecoration.UNDERLINED);
			}
			return style;
		}
	}

	public static String getLastCode(String s) {
		String ts = "";
		char[] ch = s.toCharArray();
		for (int a = 0; a < s.length() - 1; a++) {
			if (String.valueOf(ch[a + 1]).matches("[lkomnLKOMN]") && ch[a] == BUKKIT_COLOR_CODE_PREFIX_CHAR) {
				ts += String.valueOf(ch[a]) + ch[a + 1];
				a++;
			} else if (String.valueOf(ch[a + 1]).matches("[0123456789abcdefrABCDEFR]")
					&& ch[a] == BUKKIT_COLOR_CODE_PREFIX_CHAR) {
				ts = String.valueOf(ch[a]) + ch[a + 1];
				a++;
			} else if (ch[a + 1] == 'x' && ch[a] == BUKKIT_COLOR_CODE_PREFIX_CHAR) {
				if (ch.length > a + 13) {
					if (String.valueOf(ch[a + 3]).matches("[0123456789abcdefABCDEF]")
							&& String.valueOf(ch[a + 5]).matches("[0123456789abcdefABCDEF]")
							&& String.valueOf(ch[a + 7]).matches("[0123456789abcdefABCDEF]")
							&& String.valueOf(ch[a + 9]).matches("[0123456789abcdefABCDEF]")
							&& String.valueOf(ch[a + 11]).matches("[0123456789abcdefABCDEF]")
							&& String.valueOf(ch[a + 13]).matches("[0123456789abcdefABCDEF]")
							&& ch[a + 2] == BUKKIT_COLOR_CODE_PREFIX_CHAR && ch[a + 4] == BUKKIT_COLOR_CODE_PREFIX_CHAR
							&& ch[a + 6] == BUKKIT_COLOR_CODE_PREFIX_CHAR && ch[a + 8] == BUKKIT_COLOR_CODE_PREFIX_CHAR
							&& ch[a + 10] == BUKKIT_COLOR_CODE_PREFIX_CHAR
							&& ch[a + 12] == BUKKIT_COLOR_CODE_PREFIX_CHAR) {
						ts = String.valueOf(ch[a]) + ch[a + 1] + ch[a + 2] + ch[a + 3] + ch[a + 4] + ch[a + 5]
								+ ch[a + 6] + ch[a + 7] + ch[a + 8] + ch[a + 9] + ch[a + 10] + ch[a + 11] + ch[a + 12]
								+ ch[a + 13];
						a += 13;
					}
				}
			}
		}
		return ts;
	}

	@SuppressWarnings("unchecked")
	public static String toColoredText(Object o, Class<?> c) {
		if (VersionHandler.is1_7()) {
			return "\"extra\":[{\"text\":\"Hover to see original message is not currently supported in 1.7\",\"color\":\"red\"}]";
		} 
		List<Object> finalList = new ArrayList<>();
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append("\"extra\":[");
		try {
			splitComponents(finalList, o, c);
			for (Object component : finalList) {		
				try {
					if (VersionHandler.is1_8() || VersionHandler.is1_9() || VersionHandler.is1_10() || VersionHandler.is1_11() || VersionHandler.is1_12() || VersionHandler.is1_13() || VersionHandler.is1_14() || VersionHandler.is1_15() || VersionHandler.is1_16() || VersionHandler.is1_17()) {
						String text = (String) component.getClass().getMethod("getText").invoke(component);
						Object chatModifier = component.getClass().getMethod("getChatModifier").invoke(component);
						Object color = chatModifier.getClass().getMethod("getColor").invoke(chatModifier);
						String colorString = "white";
						if (color != null ) {
							colorString = color.getClass().getMethod("b").invoke(color).toString();
						}
						boolean bold = (boolean) chatModifier.getClass().getMethod("isBold").invoke(chatModifier);
						boolean strikethrough = (boolean) chatModifier.getClass().getMethod("isStrikethrough").invoke(chatModifier);
						boolean italic = (boolean) chatModifier.getClass().getMethod("isItalic").invoke(chatModifier);
						boolean underlined = (boolean) chatModifier.getClass().getMethod("isUnderlined").invoke(chatModifier);
						boolean obfuscated = (boolean) chatModifier.getClass().getMethod("isRandom").invoke(chatModifier);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("text", text);
						jsonObject.put("color", colorString);
						jsonObject.put("bold", bold);
						jsonObject.put("strikethrough", strikethrough);
						jsonObject.put("italic", italic);
						jsonObject.put("underlined", underlined);
						jsonObject.put("obfuscated", obfuscated);
						stringbuilder.append(jsonObject.toJSONString() + ",");
					} else {
						String text = (String) component.getClass().getMethod("getString").invoke(component);
						Object chatModifier = component.getClass().getMethod("c").invoke(component);
						Object color = chatModifier.getClass().getMethod("a").invoke(chatModifier);
						String colorString = "white";
						if (color != null ) {
							colorString = color.getClass().getMethod("b").invoke(color).toString();
						}
						boolean bold = (boolean) chatModifier.getClass().getMethod("b").invoke(chatModifier);
						boolean italic = (boolean) chatModifier.getClass().getMethod("c").invoke(chatModifier);
						boolean strikethrough = (boolean) chatModifier.getClass().getMethod("d").invoke(chatModifier);
						boolean underlined = (boolean) chatModifier.getClass().getMethod("e").invoke(chatModifier);
						boolean obfuscated = (boolean) chatModifier.getClass().getMethod("f").invoke(chatModifier);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("text", text);
						jsonObject.put("color", colorString);
						jsonObject.put("bold", bold);
						jsonObject.put("strikethrough", strikethrough);
						jsonObject.put("italic", italic);
						jsonObject.put("underlined", underlined);
						jsonObject.put("obfuscated", obfuscated);
						stringbuilder.append(jsonObject.toJSONString() + ",");
					}
				}
				catch(Exception e) {
					return "\"extra\":[{\"text\":\"Something went wrong. Could not access color.\",\"color\":\"red\"}]";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String coloredText = stringbuilder.toString();
		if(coloredText.endsWith(",")) {
			coloredText = coloredText.substring(0, coloredText.length() - 1);
		}
		coloredText += "]";
		return coloredText;
	}

	public static String toPlainText(Object o, Class<?> c) {
		List<Object> finalList = new ArrayList<>();
		StringBuilder stringbuilder = new StringBuilder();
		try {
			splitComponents(finalList, o, c);
			for (Object component : finalList) {
				if (VersionHandler.is1_7()) {
					stringbuilder.append((String) component.getClass().getMethod("e").invoke(component));
				} else if(VersionHandler.is1_8() || VersionHandler.is1_9() || VersionHandler.is1_10() || VersionHandler.is1_11() || VersionHandler.is1_12() || VersionHandler.is1_13() || VersionHandler.is1_14() || VersionHandler.is1_15() || VersionHandler.is1_16() || VersionHandler.is1_17()){
					stringbuilder.append((String) component.getClass().getMethod("getText").invoke(component));
				}
				else {
					stringbuilder.append((String) component.getClass().getMethod("getString").invoke(component));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringbuilder.toString();
	}

	private static void splitComponents(List<Object> finalList, Object o, Class<?> c) throws Exception {
		if (VersionHandler.is1_7() || VersionHandler.is1_8() || VersionHandler.is1_9() || VersionHandler.is1_10()
				|| VersionHandler.is1_11() || VersionHandler.is1_12() || VersionHandler.is1_13()
				|| (VersionHandler.is1_14() && !VersionHandler.is1_14_4())) {
			ArrayList<?> list = (ArrayList<?>) c.getMethod("a").invoke(o, new Object[0]);
			for (Object component : list) {
				ArrayList<?> innerList = (ArrayList<?>) c.getMethod("a").invoke(component, new Object[0]);
				if (innerList.size() > 0) {
					splitComponents(finalList, component, c);
				} else {
					finalList.add(component);
				}
			}
		} else if(VersionHandler.is1_14_4() || VersionHandler.is1_15() || VersionHandler.is1_16() || VersionHandler.is1_17()) {
			ArrayList<?> list = (ArrayList<?>) c.getMethod("getSiblings").invoke(o, new Object[0]);
			for (Object component : list) {
				ArrayList<?> innerList = (ArrayList<?>) c.getMethod("getSiblings").invoke(component, new Object[0]);
				if (innerList.size() > 0) {
					splitComponents(finalList, component, c);
				} else {
					finalList.add(component);
				}
			}
		}
		else {
			ArrayList<?> list = (ArrayList<?>) c.getMethod("b").invoke(o, new Object[0]);
			for (Object component : list) {
				ArrayList<?> innerList = (ArrayList<?>) c.getMethod("b").invoke(component, new Object[0]);
				if (innerList.size() > 0) {
					splitComponents(finalList, component, c);
				} else {
					finalList.add(component);
				}
			}
		}
	}

	/**
     * Formats a string with both Spigot legacy colors codes and Spigot and
     * VentureChat hex color codes.
     *
     * @param string to format.
     * @return {@link String}
     */
	public static String FormatStringColor(String string) {
		String allFormated = string;
		allFormated = LEGACY_CHAT_COLOR_DIGITS_PATTERN.matcher(allFormated).replaceAll("\u00A7$1");

		allFormated = allFormated.replaceAll("&[x]", BUKKIT_COLOR_CODE_PREFIX + "x");
		allFormated = allFormated.replaceAll("&[aA]", BUKKIT_COLOR_CODE_PREFIX + "a");
		allFormated = allFormated.replaceAll("&[bB]", BUKKIT_COLOR_CODE_PREFIX + "b");
		allFormated = allFormated.replaceAll("&[cC]", BUKKIT_COLOR_CODE_PREFIX + "c");
		allFormated = allFormated.replaceAll("&[dD]", BUKKIT_COLOR_CODE_PREFIX + "d");
		allFormated = allFormated.replaceAll("&[eE]", BUKKIT_COLOR_CODE_PREFIX + "e");
		allFormated = allFormated.replaceAll("&[fF]", BUKKIT_COLOR_CODE_PREFIX + "f");

		allFormated = allFormated.replaceAll("%", "\\%");

		allFormated = convertHexColorCodeStringToBukkitColorCodeString(allFormated);
		return allFormated;
	}

	/**
     * Formats a string with only legacy Spigot color codes &[0-9a-f]. Does not
     * format the legacy color codes that make up a Spigot hex color code.
     *
     * @param string to format.
     * @return {@link String}
     */
	public static String FormatStringLegacyColor(String string) {
		String allFormated = string;

		allFormated = LEGACY_CHAT_COLOR_PATTERN.matcher(allFormated).replaceAll("\u00A7$13");
		allFormated = allFormated.replaceAll(BUKKIT_COLOR_CODE_PREFIX + "[A]", BUKKIT_COLOR_CODE_PREFIX + "a");
		allFormated = allFormated.replaceAll(BUKKIT_COLOR_CODE_PREFIX + "[B]", BUKKIT_COLOR_CODE_PREFIX + "b");
		allFormated = allFormated.replaceAll(BUKKIT_COLOR_CODE_PREFIX + "[C]", BUKKIT_COLOR_CODE_PREFIX + "c");
		allFormated = allFormated.replaceAll(BUKKIT_COLOR_CODE_PREFIX + "[D]", BUKKIT_COLOR_CODE_PREFIX + "d");
		allFormated = allFormated.replaceAll(BUKKIT_COLOR_CODE_PREFIX + "[E]", BUKKIT_COLOR_CODE_PREFIX + "e");
		allFormated = allFormated.replaceAll(BUKKIT_COLOR_CODE_PREFIX + "[F]", BUKKIT_COLOR_CODE_PREFIX + "f");

		allFormated = allFormated.replaceAll("%", "\\%");
		return allFormated;
	}

	/**
     * Formats a string with Spigot formatting codes.
     *
     * @param string to format.
     * @return {@link String}
     */
	public static String FormatString(String string) {
		String allFormated = string;
		allFormated = allFormated.replaceAll("&[kK]", BUKKIT_COLOR_CODE_PREFIX + "k");
		allFormated = allFormated.replaceAll("&[lL]", BUKKIT_COLOR_CODE_PREFIX + "l");
		allFormated = allFormated.replaceAll("&[mM]", BUKKIT_COLOR_CODE_PREFIX + "m");
		allFormated = allFormated.replaceAll("&[nN]", BUKKIT_COLOR_CODE_PREFIX + "n");
		allFormated = allFormated.replaceAll("&[oO]", BUKKIT_COLOR_CODE_PREFIX + "o");
		allFormated = allFormated.replaceAll("&[rR]", BUKKIT_COLOR_CODE_PREFIX + "r");

		allFormated = allFormated.replaceAll("%", "\\%");
		return allFormated;
	}

	/**
     * Formats a string with Spigot legacy colors codes, Spigot and VentureChat hex
     * color codes, and Spigot formatting codes.
     *
     * @param string to format.
     * @return {@link String}
     */
	public static String FormatStringAll(String string) {
		String allFormated = Format.FormatString(string);
		allFormated = Format.FormatStringColor(allFormated);
		return allFormated;
	}

	public static boolean startsWithColorCode(String text) {
		return text != null && LEADING_COLOR_CODE_PATTERN.matcher(text).find();
	}

	public static String FilterChat(String msg) {
		int t = 0;
		List<String> filters = getInstance().getConfig().getStringList("filters");
		for (String s : filters) {
			t = 0;
			String[] pparse = new String[2];
			pparse[0] = " ";
			pparse[1] = " ";
			StringTokenizer st = new StringTokenizer(s, ",");
			while (st.hasMoreTokens()) {
				if (t < 2) {
					pparse[t++] = st.nextToken();
				}
			}
			// (?i) = case insensitive
			msg = msg.replaceAll("(?i)" + pparse[0], pparse[1]);
		}
		return msg;
	}

	public static boolean isValidColor(String color) {
		Boolean bFound = false;
		for (ChatColor bkColors : ChatColor.values()) {
			if (color.equalsIgnoreCase(bkColors.name())) {
				bFound = true;
			}
		}
		return bFound;
	}

	/**
     * Validates a hex color code.
     *
     * @param color to validate.
     * @return true if color code is valid, false otherwise.
     */
	public static boolean isValidHexColor(String color) {
		Pattern pattern = Pattern.compile("(^&?#[0-9a-fA-F]{6}\\b)");
		Matcher matcher = pattern.matcher(color);
		return matcher.find();
	}

	/**
     * Convert a single hex color code to a single Bukkit hex color code.
     *
     * @param color to convert.
     * @return {@link String}
     */
	public static String convertHexColorCodeToBukkitColorCode(String color) {
		color = color.replace("&", "");
		StringBuilder bukkitColorCode = new StringBuilder(BUKKIT_COLOR_CODE_PREFIX + BUKKIT_HEX_COLOR_CODE_PREFIX);
		for (int a = 1; a < color.length(); a++) {
			bukkitColorCode.append(BUKKIT_COLOR_CODE_PREFIX + color.charAt(a));
		}
		return bukkitColorCode.toString().toLowerCase();
	}

	/**
     * Convert an entire String of hex color codes to Bukkit hex color codes.
     *
     * @param string to convert.
     * @return {@link String}
     */
	public static String convertHexColorCodeStringToBukkitColorCodeString(String string) {
		Pattern pattern = Pattern.compile("(&?#[0-9a-fA-F]{6})");
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			int indexStart = matcher.start();
			int indexEnd = matcher.end();
			String hexColor = string.substring(indexStart, indexEnd);
			String bukkitColor = convertHexColorCodeToBukkitColorCode(hexColor);
			string = string.replaceAll(hexColor, bukkitColor);
			matcher.reset(string);
		}
		return string;
	}

	public static String underlineURLs() {
		final boolean configValue = getInstance().getConfig().getBoolean("underlineurls", true);
		if (VersionHandler.isAtLeast_1_20_4()) {
			return String.valueOf(configValue);
		} else {
			return "\"" + configValue + "\"";
		}
	}
	
	public static String parseTimeStringFromMillis(long millis) {
		String timeString = "";
		if(millis >= Format.MILLISECONDS_PER_DAY) {
			long numberOfDays = millis / Format.MILLISECONDS_PER_DAY;
			millis -= Format.MILLISECONDS_PER_DAY * numberOfDays;
			
			String units = LocalizedMessage.UNITS_DAY_PLURAL.toString();
			if (numberOfDays == 1) {
				units = LocalizedMessage.UNITS_DAY_SINGULAR.toString();
			}
			timeString += numberOfDays + " " + units + " ";
		}
		
		if(millis >= Format.MILLISECONDS_PER_HOUR) {
			long numberOfHours = millis / Format.MILLISECONDS_PER_HOUR;
			millis -= Format.MILLISECONDS_PER_HOUR * numberOfHours;

			String units = LocalizedMessage.UNITS_HOUR_PLURAL.toString();
			if (numberOfHours == 1) {
				units = LocalizedMessage.UNITS_HOUR_SINGULAR.toString();
			}
			timeString += numberOfHours + " " + units + " ";
		}
		
		if(millis >= Format.MILLISECONDS_PER_MINUTE) {
			long numberOfMinutes = millis / Format.MILLISECONDS_PER_MINUTE;
			millis -= Format.MILLISECONDS_PER_MINUTE * numberOfMinutes;

			String units = LocalizedMessage.UNITS_MINUTE_PLURAL.toString();
			if (numberOfMinutes == 1) {
				units = LocalizedMessage.UNITS_MINUTE_SINGULAR.toString();
			}
			timeString += numberOfMinutes + " " + units + " ";
		}
		
		if(millis >= Format.MILLISECONDS_PER_SECOND) {
			long numberOfSeconds = millis / Format.MILLISECONDS_PER_SECOND;
			millis -= Format.MILLISECONDS_PER_SECOND * numberOfSeconds;

			String units = LocalizedMessage.UNITS_SECOND_PLURAL.toString();
			if (numberOfSeconds == 1) {
				units = LocalizedMessage.UNITS_SECOND_SINGULAR.toString();
			}
			timeString += numberOfSeconds + " " + units;
		}
		return timeString.trim();
	}

	public static void broadcastToServer(CommandSender sender, String message) {
		if (message == null || message.isBlank()) {
			return;
		}
		String formattedMessage = FormatStringAll(message);
		UUID senderUuid = sender instanceof Player player
				? player.getUniqueId()
				: null;

		for (MineverseChatPlayer mcp : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
			Player target = mcp.getPlayer();

			if (target == null || !target.isOnline()) {
				continue;
			}
			if(MineverseChat.getInstance().getConfig().getBoolean("ignoreBroadCast", false)){
				if (senderUuid != null && mcp.getIgnores().contains(senderUuid)) {
					continue;
				}
			}

			target.sendMessage(formattedMessage);
		}
	}

	public static String processPlaceHolders(CommandSender sender, String string){
		if (string == null || string.isBlank()) {
			return string;
		}
		if (sender instanceof Player player) {
			return PlaceholderAPI.setPlaceholders(player, string);
		}

		return string;
	}


	public static void playMessageSound(MineverseChatPlayer mcp) {
		Player player = mcp.getPlayer();
		String soundName = getInstance().getConfig().getString("message_sound", DEFAULT_MESSAGE_SOUND);
		if (!soundName.equalsIgnoreCase("None")) {
			try {
				Sound messageSound = getSound(soundName);
				player.playSound(player.getLocation(), messageSound, 1, 0);
			} catch (final Exception e) {
				if (MineverseChat.getInstance().getConfig().getString("loglevel", "info").equals("debug")) {
					Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Error playing sound, defaulting to none"));
				}
			}
		}
	}
	
	private static Sound getSound(String soundName) {
		if (soundName == null || soundName.trim().isEmpty()) {
			return getDefaultMessageSound();
		}
		NamespacedKey key = NamespacedKey.fromString(soundName.toLowerCase(Locale.ROOT));
		if (key != null) {
			Sound sound = Registry.SOUNDS.get(key);
			if (sound != null) {
				return sound;
			}
		}
		Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Message sound invalid!"));
		return getDefaultMessageSound();
	}
	
	private static Sound getDefaultMessageSound() {
		NamespacedKey key;
		if(VersionHandler.is1_7() || VersionHandler.is1_8()) {
			key = NamespacedKey.fromString(DEFAULT_LEGACY_MESSAGE_SOUND);
			if (key != null) {
                return Registry.SOUNDS.get(key);
			}
		}
		else {
			key = NamespacedKey.fromString(DEFAULT_MESSAGE_SOUND);
			if (key != null) {
                return Registry.SOUNDS.get(key);
			}
		}
        return null;
    }
	
	public static String stripColor(String message) {
		return message.replaceAll("(\u00A7([a-z0-9]))", "");
	}
}
