package com.ericlam.mc.placeholder.addon;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderExtender extends PlaceholderExpansion {

    private final Plugin plugin;
    private final PlaceholderManager manager;

    PlaceholderExtender(Plugin plugin, PlaceholderManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        String[] para = params.split("_");
        if (para.length != 2) return null;
        return manager.getPlaceholder(p.getUniqueId(), para[0], para[1]);
    }

    @Override
    public String getIdentifier() {
        return "papiaddon";
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
