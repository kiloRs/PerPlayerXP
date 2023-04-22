package com.profilesplus;

import com.profilesplus.players.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class PlayerMoveRestrict implements Listener {

    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();


        SpectatorManager manager = ((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager();
        if (manager.isWaiting(player)) {
            Location spectatorLocation =  manager.getSpectatorLocation();
            if (spectatorLocation == null){
                spectatorLocation = event.getFrom();
            }
            event.setTo(spectatorLocation);

        }
    }
}