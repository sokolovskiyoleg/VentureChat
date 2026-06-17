package mineverse.Aust1n46.chat.localization;

import java.io.File;
import java.util.Locale;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.utilities.Format;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

//This class is used to create objects of localization for different languages.
public class Localization { 
	private static final String DEFAULT_LANGUAGE = "en-us";
	private static FileConfiguration localization;

	public static void initialize() {
		MineverseChat plugin = MineverseChat.getInstance();
		String configuredLanguage = plugin.getConfig().getString("language", DEFAULT_LANGUAGE);
		String language = normalizeLanguage(configuredLanguage);
		File localizationFile = resolveLanguageFile(plugin, language);

		if (localizationFile == null && !DEFAULT_LANGUAGE.equals(language)) {
			Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Missing language '" + configuredLanguage + "', falling back to en-us"));
			localizationFile = resolveLanguageFile(plugin, DEFAULT_LANGUAGE);
		}

		if (localizationFile == null) {
			throw new IllegalStateException("Unable to load VentureChat language files.");
		}

		localization = YamlConfiguration.loadConfiguration(localizationFile);
	}

	static String normalizeLanguage(String language) {
		if (language == null || language.trim().isEmpty()) {
			return DEFAULT_LANGUAGE;
		}
		return language.trim().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	static String getResourceName(String language) {
		String normalized = normalizeLanguage(language);
		return "language" + "/" + normalized + ".yml";
	}

	static File resolveLanguageFile(MineverseChat plugin, String language) {
		String resourceName = getResourceName(language);
		File dataFile = new File(plugin.getDataFolder(), resourceName);
		File parent = dataFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		if (dataFile.isFile()) {
			return dataFile;
		}
		if (plugin.getResource(resourceName) == null) {
			return null;
		}
		plugin.saveResource(resourceName, false);
		return dataFile;
	}

	public static FileConfiguration getLocalization() {
		return localization;
	}
}