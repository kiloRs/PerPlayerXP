package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import net.Indyuce.mmocore.api.event.AsyncPlayerDataLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onPlayerJoin(AsyncPlayerDataLoadEvent event) {
        PlayerData.get(event.getPlayer()).loadProfiles();
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        playerData.saveProfiles();
    }

    public void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                RPGProfiles.log("AutoSave has been set for " + saveInterval + " minutes!");
                for (PlayerData playerData : PlayerData.getAllInstances()) {
                    playerData.saveProfiles();
                }
            }
        }.runTaskTimerAsynchronously(plugin, saveInterval * 60 * 20L, saveInterval * 60 * 20L);
    }
}
