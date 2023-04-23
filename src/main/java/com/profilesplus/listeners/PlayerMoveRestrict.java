package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.SpectatorManager;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveRestrict implements Listener {

    private final SpectatorManager spectatorManager;
    private double aDouble;

    public PlayerMoveRestrict(SpectatorManager spectatorManager) {
        this.spectatorManager = spectatorManager;
    }
    @EventHandler
    public void on(PlayerInteractEvent event){
        if (spectatorManager.isWaiting(event.getPlayer())){
            if (event.getAction().isRightClick()){
                event.setCancelled(true);
                new ProfilesMenu(RPGProfiles.getInstance(), PlayerData.get(event.getPlayer())).open();
            }
            else {
                event.getPlayer().sendMessage("You must select or create a profile to continue!");
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (spectatorManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (spectatorManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
        }

    }
}