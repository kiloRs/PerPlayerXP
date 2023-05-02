package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.ActiveKeyHolder;
import com.profilesplus.players.PlayerData;
import net.Indyuce.mmocore.api.event.AsyncPlayerDataLoadEvent;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDataListener implements Listener {
    private final JavaPlugin plugin;
    private final int saveInterval;

    public PlayerDataListener(JavaPlugin plugin, int saveInterval) {
        this.plugin = plugin;
        this.saveInterval = saveInterval;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoad(AsyncPlayerDataLoadEvent event) {
        new BukkitRunnable(){
            @Override
            public void run() {
                PlayerData playerData = PlayerData.get(event.getPlayer());

                playerData.loadProfiles();

                if (!playerData.getProfileStorage().hasActiveProfile()){

                    if (new ActiveKeyHolder(event.getPlayer()).hasActive()){
                        if (playerData.loadActive()) {
                            RPGProfiles.log("LOADED ACTIVE PROFILE OF " + event.getPlayer().getName().toUpperCase());
                            return;
                        }
                        //toDo Fix
                    }
                    RPGProfiles.getLimboManager().setWaiting(event.getPlayer(),event.getPlayer().getWorld().getSpawnLocation().toBlockLocation());
                    return;
                }




            }
        }.runTaskLater(RPGProfiles.getInstance(),1);
    }

    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());

        if (playerData.getProfileStorage().hasActiveProfile()) {
            playerData.saveActiveProfile(true);
        }
        else {
            if (playerData.getKeyHolder().hasActive()){
                playerData.getKeyHolder().resetActive();
            }
            RPGProfiles.log("No Profiles to Save for Player (" + event.getPlayer().getName() + ")");
        }

    }
    @EventHandler
    public void on(PlayerTeleportEvent teleportEvent){
        if (teleportEvent.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN){
            return;
        }
        PlayerData playerData = PlayerData.get(teleportEvent.getPlayer());
        if (!playerData.getProfileStorage().hasActiveProfile()){
            return;
        }
        try {
            playerData.saveActiveProfile(playerData.getProfileStorage().getActiveProfile().isCreated());
            RPGProfiles.log("Saved Via Teleport Action of " + teleportEvent.getPlayer().getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @EventHandler
    public void onExperience(PlayerExperienceGainEvent event){
        if (!event.hasProfession()){
            if (event.isCancelled()){
                return;
            }
            PlayerData playerData = PlayerData.get(event.getPlayer());
            if (!playerData.getProfileStorage().hasActiveProfile()){
                return;
            }
            playerData.saveActiveProfile(true);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event){
        PlayerData playerData = PlayerData.get(event.getPlayer());
        playerData.saveActiveProfile(true);
    }
    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void death(PlayerDeathEvent event){
        PlayerData playerData = PlayerData.get(event.getPlayer());
        playerData.saveActiveProfile(true);
    }
    @EventHandler(ignoreCancelled = true)
    public void onLevel(PlayerLevelUpEvent event){
        if (event.hasProfession()){
        PlayerData playerData = PlayerData.get(event.getPlayer());

        playerData.saveActiveProfile(true);
    }
    }
    @EventHandler
    public void onLoad(ServerLoadEvent event){
        if (event.getType()== ServerLoadEvent.LoadType.RELOAD){
            new BukkitRunnable(){
                @Override
                public void run() {
                    for (Player player:Bukkit.getOnlinePlayers()) {
                        if (player.isOnline()){
                            if (net.Indyuce.mmocore.api.player.PlayerData.has(player)){
                                PlayerData playerData = PlayerData.get(player);
                                playerData.saveActiveProfile(true);
                            }
                            else {
                                throw new RuntimeException("MMOCore Player not loaded yet!");
                            }
                        }
                    }
                }
            }.runTaskLaterAsynchronously(RPGProfiles.getInstance(),10);

        }
    }
    public void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                RPGProfiles.log("AutoSave has been set for " + saveInterval + " minutes!");
                if (Bukkit.getOnlinePlayers().isEmpty()){
                    RPGProfiles.debug("Save Skip - No Online Players !");
                }
                for (Player p: Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().toBlockLocation().equals(RPGProfiles.getLimboManager().getSpectatorLocation().toBlockLocation()) || RPGProfiles.getLimboManager().isWaiting(p)){
                        RPGProfiles.debug("Skipping Saving Limbo Player: " + p.getName());
                        continue;
                    }
                    if (PlayerData.exists(p)) {
                        PlayerData playerData = PlayerData.get(p);
                        if (!playerData.getProfileStorage().hasActiveProfile()){
                            return;
                        }
                        if (playerData.getPlayer().isOnline()) {
                            if (playerData.getProfileStorage().getActiveProfile().isCreated()) {
                                playerData.saveActiveProfile(true);
                                continue;
                            }
                            playerData.saveActiveProfile(false);

                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, saveInterval * 60 * 20L, saveInterval * 60 * 20L);
    }
}
