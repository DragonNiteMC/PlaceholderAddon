package com.ericlam.mc.placeholder.addon;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerUpdateRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final ConcurrentLinkedQueue<String> games = new ConcurrentLinkedQueue<>();
    private final PlaceholderManager placeholderManager;
    private final UUID player;
    private final PapiAddonConfig config;

    public PlayerUpdateRunnable(PapiAddon addon, UUID player, PapiAddonConfig config) {
        this.plugin = addon;
        this.placeholderManager = addon.getManager();
        this.player = player;
        this.config = config;
    }

    @Override
    public void run() {
        config.placeholders.keySet().forEach(game -> {
            games.offer(game);
            new PlaceholderUpdateRunnable(placeholderManager, player, games).runTaskAsynchronously(plugin);
        });
    }
}
