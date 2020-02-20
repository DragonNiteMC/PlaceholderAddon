package com.ericlam.mc.placeholder.addon;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BungeePlaceholder extends PlaceholderExpansion implements PluginMessageListener {

    private final Plugin plugin;
    private static final String PLAYERS_COUNT_DEFAULT = "0";
    private final Map<String, Integer> playerCount = new ConcurrentHashMap<>();

    BungeePlaceholder(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        String[] servers = params.split(":");
        int result = 0;
        for (String server : servers) {
            this.playerCount.putIfAbsent(server, 0);
            result += this.playerCount.get(server);
        }
        return result+"";
    }


    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessageReceived(@Nonnull String s, @Nonnull Player player, @Nonnull byte[] bytes) {
        if (!s.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();
        if (subchannel.equals("PlayerCount")) {
            String server = in.readUTF();
            int players = in.readInt();
            this.playerCount.put(server, players);
        }
    }

    @Override
    public String getIdentifier() {
        return "pa-bungee";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

}
