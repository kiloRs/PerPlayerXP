package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.menu.CharSelectionMenu;
import com.profilesplus.players.PlayerData;
import net.Indyuce.mmocore.api.event.PlayerDataLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());

        if (playerData.getConfig().getKeys(false).isEmpty()) {
            RPGProfiles.log("&cNo player information for " + event.getPlayer().getName() + "'s configuration!");

            new CharSelectionMenu(playerData).open();
            return;
        }

        playerData.loadProfiles();
    }

    @EventHandler
    public void onRPG(PlayerDataLoadEvent event){
        PlayerData playerData = PlayerData.get(event.getPlayer());
        CharSelectionMenu menu = new CharSelectionMenu(playerData);

        if (!playerData.hasActiveKey() || playerData.getProfileMap().isEmpty() || playerData.getActiveProfile() == null){
            menu.open();
        }
    }
    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());

        if (!playerData.getProfileMap().isEmpty()) {
            playerData.saveProfiles();
        }
        else {
            RPGProfiles.log("No Profiles to Save for Player (" + event.getPlayer().getName() + ")");
        }

    }
    @EventHandler
    public void on(PlayerTeleportEvent teleportEvent){
        PlayerData playerData = PlayerData.get(teleportEvent.getPlayer());

        playerData.saveProfiles();
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        PlayerData playerData = PlayerData.get(event.getPlayer());

        playerData.saveProfiles();
    }
    @EventHandler
    public void death(PlayerDeathEvent event){
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
