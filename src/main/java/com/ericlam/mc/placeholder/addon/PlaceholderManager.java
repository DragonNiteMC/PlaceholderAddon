package com.ericlam.mc.placeholder.addon;

import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.SQLDataSource;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class PlaceholderManager {
    private Map<String, PapiAddonConfig.Placeholder> placeholderMap;
    private Map<UUID, Map<String, String>> gameStats = new HashMap<>();
    private SQLDataSource sqlDataSource;

    PlaceholderManager(PapiAddonConfig config) {
        this.sqlDataSource = HyperNiteMC.getAPI().getSQLDataSource();
        this.placeholderMap = config.placeholders;
    }

    // this.getPlaceholder("gsg", "kills") --> %papiaddon_gsg_kills%
    String getPlaceholder(UUID player, String game, String stats) {
        return Optional.ofNullable(gameStats.get(player)).map(map -> map.get(game + "_" + stats)).orElse("0");
    }

    CompletableFuture<Void> refreshData() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection()) {
                ConcurrentLinkedQueue<UUID> uuids = new ConcurrentLinkedQueue<>(gameStats.keySet());
                while (!uuids.isEmpty()) {
                    UUID uuid = uuids.poll();
                    this.takeFromSQL(connection, uuid);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    CompletableFuture<Void> refreshData(@Nonnull UUID uuid) {
        return CompletableFuture.runAsync(() -> takeFromSQL(uuid));
    }

    //On every join
    void takeFromSQL(@Nonnull UUID player) {
        try (Connection connection = sqlDataSource.getConnection()) {
            this.takeFromSQL(connection, player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void takeFromSQL(@Nonnull Connection connection, @Nonnull UUID player) throws SQLException {
        ConcurrentLinkedQueue<Map.Entry<String, PapiAddonConfig.Placeholder>> hnPlaceholders = new ConcurrentLinkedQueue<>(this.placeholderMap.entrySet());
        while (!hnPlaceholders.isEmpty()) {
            var entry = hnPlaceholders.poll();
            final String game = entry.getKey();
            PapiAddonConfig.Placeholder placeholder = entry.getValue();
            final String table = placeholder.table;
            final String uuid = placeholder.uuid;
            final List<PapiAddonConfig.Stats> stats = placeholder.getColumns();
            if (stats.size() == 0) throw new IllegalStateException("Stats columns size is 0");
            final String b = stats.stream().map(st -> st.column).collect(Collectors.toList()).toString().replace("[", "").replace("]", "");
            final String stmt2 = "SELECT " + b + " FROM " + table + " WHERE " + uuid + "=?";
            try (PreparedStatement select = connection.prepareStatement(stmt2);) {
                select.setString(1, player.toString());
                ResultSet set = select.executeQuery();
                Map<String, String> result = new HashMap<>();
                final boolean hasData = set.next();
                for (PapiAddonConfig.Stats stat : stats) {
                    String[] plus = stat.column.split("\\+");
                    if (plus.length < 2) {
                        result.put(game + "_" + stat.placeholder, hasData ? set.getObject(stat.column).toString() : stat.def);
                    }else{
                        result.put(game + "_" + stat.placeholder, (Integer.parseInt(result.get(game+"_"+plus[0])) + Integer.parseInt(result.get(game+"_"+plus[1]))+""));
                    }
                }
                this.gameStats.put(player, result);
            }
        }
    }


}
