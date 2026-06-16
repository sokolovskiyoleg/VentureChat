package mineverse.Aust1n46.chat.alias;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import mineverse.Aust1n46.chat.MineverseChat;

public class Alias extends Command {
    private static MineverseChat plugin = MineverseChat.getInstance();
    private static List<Alias> aliases;

    private int arguments;
    private List<String> components;
    private String permission;

    public Alias(String name, int arguments, List<String> components, String permission) {
        super(name);
        this.arguments = arguments;
        this.components = components;
        this.permission = "venturechat." + permission;
    }

    public static void initialize() {
        aliases = new ArrayList<Alias>();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("alias");
        CommandMap commandMap = getCommandMap(plugin.getServer());
        for (String key : cs.getKeys(false)) {
            String name = key;
            int arguments = cs.getInt(key + ".arguments", 0);
            List<String> components = cs.getStringList(key + ".components");
            String permissions = cs.getString(key + ".permissions", "None");
            Alias alias = new Alias(name, arguments, components, permissions);
            aliases.add(alias);
            commandMap.register("venturechat", alias);
        }
    }

    private static CommandMap getCommandMap(Server server) {
        try {
            return server.getCommandMap();
        } catch (NoSuchMethodError e) {
            try {
                Field commandMapField = server.getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                return (CommandMap) commandMapField.get(server);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Unable to access CommandMap. Use Paper.", ex);
            }
        }
    }

    public static List<Alias> all() {
        return aliases;
    }

    public int getArguments() {
        return arguments;
    }

    public List<String> getComponents() {
        return components;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return !permission.equalsIgnoreCase("venturechat.none");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return false;
    }

	@Override
	public List<String> tabComplete(CommandSender sender, String label, String[] args) {
		return Collections.emptyList();
	}
}
