package com.ericlam.mc.placeholder.addon;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlaceholderUpdateRunnable extends BukkitRunnable {
    private final PlaceholderManager placeholderManager;
    private final UUID player;
    private final ConcurrentLinkedQueue<String> games;

    public PlaceholderUpdateRunnable(PlaceholderManager placeholderManager, UUID player, ConcurrentLinkedQueue<String> games) {
        this.placeholderManager = placeholderManager;
        this.player = player;
        this.games = games;
    }

    @Override
    public void run() {
        String game = games.poll();
        if (game != null) {
            placeholderManager.takePlaceholderFromSQL(player, game);
        }
    }
}
