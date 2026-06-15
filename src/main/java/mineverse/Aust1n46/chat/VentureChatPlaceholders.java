package mineverse.Aust1n46.chat;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;

public class VentureChatPlaceholders extends PlaceholderExpansion {
    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) {
            return null;
        }
        MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(p);
        if (mcp == null) {
            return "";
        }
        if (identifier.equalsIgnoreCase("nickname")) {
            return mcp.hasNickname() ? mcp.getNickname() : mcp.getName();
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Aust1n46";
    }

    @Override
    public String getIdentifier() {
        return "venturechat";
    }

    @Override
    public String getVersion() {
        return MineverseChat.getInstance().getDescription().getVersion();
    }
}
