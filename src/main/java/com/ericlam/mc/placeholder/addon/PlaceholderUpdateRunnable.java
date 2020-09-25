package com.ericlam.mc.placeholder.addon;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class PlaceholderUpdateRunnable extends BukkitRunnable {
    private final Consumer<UpdateAction> asyncRun;
    private final ConcurrentLinkedQueue<UpdateAction> actions;

    public PlaceholderUpdateRunnable(Consumer<UpdateAction> asyncRun, ConcurrentLinkedQueue<UpdateAction> actions) {
        this.asyncRun = asyncRun;
        this.actions = actions;
    }

    @Override
    public void run() {
        UpdateAction action = actions.poll();
        if (action != null) asyncRun.accept(action);
    }
}
