package mineverse.Aust1n46.chat.command;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.command.Command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.command.message.Message;

public final class PaperCommand {
	private PaperCommand() {
		throw new IllegalStateException("Utility class");
	}

	public static void registerMessageCommands(final MineverseChat plugin, final Set<String> messageAliases) {
		final Set<String> labels = new LinkedHashSet<>();
		labels.add("message");
		if (messageAliases != null) {
			labels.addAll(messageAliases);
		}
		final Message messageCommand = new Message();
		plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
			final Commands commands = event.registrar();
			for (final String label : labels) {
				commands.register(label, createBasicCommand(messageCommand, label));
			}
		});
	}

	private static BasicCommand createBasicCommand(final Command command, final String label) {
		return new BasicCommand() {
			@Override
			public void execute(final CommandSourceStack source, final String[] args) {
				command.execute(source.getSender(), label, args);
			}

			@Override
			public Collection<String> suggest(final CommandSourceStack source, final String[] args) {
				final Collection<String> suggestions = command.tabComplete(source.getSender(), label, args);
				return suggestions == null ? Collections.emptyList() : suggestions;
			}

			@Override
			public boolean canUse(final org.bukkit.command.CommandSender sender) {
				return command.testPermissionSilent(sender);
			}

			@Override
			public String permission() {
				final String permission = command.getPermission();
				return permission == null ? "" : permission;
			}
		};
	}
}
