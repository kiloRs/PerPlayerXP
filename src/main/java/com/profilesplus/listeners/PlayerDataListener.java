package com.profilesplus.listeners;

import com.profilesplus.players.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDataListener implements Listener {
    private final JavaPlugin plugin;
    private final int saveInterval;

    public PlayerDataListener(JavaPlugin plugin, int saveInterval) {
        this.plugin = plugin;
        this.saveInterval = saveInterval;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData.get(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        playerData.saveProfiles();
    }

    public void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerData playerData : PlayerData.getAllInstances()) {
                    playerData.saveProfiles();
                }
            }
        }.runTaskTimerAsynchronously(plugin, saveInterval * 60 * 20L, saveInterval * 60 * 20L);
    }
}
