package com.ericlam.mc.placeholder.addon;

import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.CoreConfig;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import com.hypernite.mc.hnmc.core.misc.commands.CommandNode;
import com.hypernite.mc.hnmc.core.misc.commands.CommandNodeBuilder;
import com.hypernite.mc.hnmc.core.misc.commands.DefaultCommand;
import com.hypernite.mc.hnmc.core.misc.commands.DefaultCommandBuilder;
import com.hypernite.mc.hnmc.core.misc.permission.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class PapiAddon extends JavaPlugin implements Listener {
    private PlaceholderManager manager;

    @Override
    public void onEnable() {
        YamlManager configManager = HyperNiteMC.getAPI().getFactory().getConfigFactory(this)
                .register("placeholders.yml", PapiAddonConfig.class).dump();
        PapiAddonConfig config = configManager.getConfigAs(PapiAddonConfig.class);
        manager = new PlaceholderManager(config);
        new PlaceholderExtender(this, manager).register();
        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, this.getCommand());
        this.getServer().getPluginManager().registerEvents(this, this);
        BungeePlaceholder bungeePlaceholder = new BungeePlaceholder(this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", bungeePlaceholder);
        bungeePlaceholder.register();
    }

    private DefaultCommand getCommand() {
        CoreConfig cf = HyperNiteMC.getAPI().getCoreConfig();
        CommandNode node = new CommandNodeBuilder("refresh")
                .placeholder("[player]")
                .description("強制手動更新玩家/所有玩家的 placeholder 資料")
                .alias("update", "reload")
                .permission(Perm.ADMIN)
                .execute((commandSender, list) -> {
                    if (list.size() == 0) {
                        manager.refreshData().whenComplete((v, e) -> {
                            if (e != null) {
                                commandSender.sendMessage(e.getMessage());
                                e.printStackTrace();
                            } else {
                                commandSender.sendMessage(cf.getPrefix() + ChatColor.GREEN + "資料更新成功。");
                            }
                        });
                    } else {
                        String name = list.get(0);
                        UUID uuid = Bukkit.getPlayerUniqueId(name);
                        if (uuid == null) {
                            commandSender.sendMessage(cf.getPrefix() + cf.getNotFoundPlayer());
                            return true;
                        }
                        manager.refreshData(uuid).whenComplete((v, e) -> {
                            if (e != null) {
                                commandSender.sendMessage(e.getMessage());
                                e.printStackTrace();
                            } else {
                                commandSender.sendMessage(cf.getPrefix() + ChatColor.GREEN + name + " 的資料更新成功。");
                            }
                        });
                    }
                    return true;
                }).build();

        return new DefaultCommandBuilder("papi-addon").alias("papiaddon", "papia").permission(Perm.ADMIN).description("Papi Addon 指令").children(node).build();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> manager.takeFromSQL(uuid));
    }
}
