package com.profilesplus.listeners;


import com.profilesplus.RPGProfiles;
import com.profilesplus.LimboManager;
import com.profilesplus.players.PlayerData;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        PlayerData playerData = PlayerData.get(player);
        LimboManager manager = RPGProfiles.getLimboManager();

        if (playerData.getActiveProfile() != null){
            if (playerData.getActiveProfile().update()) {
                RPGProfiles.log("Active Profile Loading Complete! " + playerData.getActiveSlot());
            }
            if (manager.isWaiting(player)) {
                manager.removeWaiting(player);
            }
        }
        else {
            RPGProfiles.log("Player is waiting...");
            Location startingLocation = player.getLocation();
            if (player.getLocation().toBlockLocation().equals(getSpectatorLocation().toBlockLocation())) {
                manager.setWaiting(player, startingLocation = player.getLocation().getWorld().getSpawnLocation());
            }
            else {
                manager.setWaiting(player,startingLocation);
            }
        }
    }


    public static Location getSpectatorLocation() {
        return RPGProfiles.getLimboManager().getSpectatorLocation();
    }
}
