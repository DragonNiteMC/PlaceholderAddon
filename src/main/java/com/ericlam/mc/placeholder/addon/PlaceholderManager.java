package com.ericlam.mc.placeholder.addon;

import com.dragonnite.mc.dnmc.core.main.DragonNiteMC;
import com.dragonnite.mc.dnmc.core.managers.SQLDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class PlaceholderManager {
    private final Map<String, PapiAddonConfig.Placeholder> placeholderMap;
    private final Map<UUID, Map<String, String>> gameStats = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<UpdateAction> updateQueue = new ConcurrentLinkedQueue<>();
    private final SQLDataSource sqlDataSource;
    private final PapiAddonConfig papiAddonConfig;
    private final PapiAddon plugin;

    PlaceholderManager(PapiAddon plugin, PapiAddonConfig papiAddonConfig) {
        this.plugin = plugin;
        this.papiAddonConfig = papiAddonConfig;
        this.sqlDataSource = DragonNiteMC.getAPI().getSQLDataSource();
        this.placeholderMap = Optional.ofNullable(papiAddonConfig.placeholders).orElse(Map.of());
    }

    // this.getPlaceholder("gsg", "kills") --> %papiaddon_gsg_kills%
    String getPlaceholder(UUID player, String game, String stats) {
        return Optional.ofNullable(gameStats.get(player)).map(map -> map.get(game + "_" + stats)).orElse("0");
    }

    void offer(UpdateAction action) {
        this.updateQueue.offer(action);
        new PlaceholderUpdateRunnable(this::takePlaceholderFromSQL, updateQueue).runTaskAsynchronously(plugin);
    }

    void refreshPlayer(UUID player) {
        papiAddonConfig.placeholders.keySet().forEach(game -> offer(new UpdateAction(player, game)));
    }

    void refreshPlayers() {
        gameStats.keySet().forEach(this::refreshPlayer);
    }

    private void takePlaceholderFromSQL(final UpdateAction action) {
        final UUID player = action.player;
        final String game = action.game;
        if (!this.placeholderMap.containsKey(game)) return;
        PapiAddonConfig.Placeholder placeholder = this.placeholderMap.get(game);
        final String table = placeholder.table;
        final String uuid = placeholder.uuid;
        final List<PapiAddonConfig.Stats> stats = placeholder.getColumns();
        if (stats.size() == 0) throw new IllegalStateException("Stats columns size is 0");
        final String b = stats.stream().map(st -> st.column).collect(Collectors.toList()).toString().replace("[", "").replace("]", "");
        final String stmt2 = "SELECT " + b + " FROM " + table + " WHERE " + uuid + "=?";
        try (Connection connection = sqlDataSource.getConnection(); PreparedStatement select = connection.prepareStatement(stmt2);) {
            select.setString(1, player.toString());
            ResultSet set = select.executeQuery();
            Map<String, String> result = new HashMap<>();
            final boolean hasData = set.next();
            for (PapiAddonConfig.Stats stat : stats) {
                String[] plus = stat.column.split("\\+");
                if (plus.length < 2) {
                    result.put(game + "_" + stat.placeholder, hasData ? set.getObject(stat.column).toString() : stat.def);
                } else {
                    result.put(game + "_" + stat.placeholder, (Integer.parseInt(result.get(game + "_" + plus[0])) + Integer.parseInt(result.get(game + "_" + plus[1])) + ""));
                }
            }
            this.gameStats.put(player, result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
