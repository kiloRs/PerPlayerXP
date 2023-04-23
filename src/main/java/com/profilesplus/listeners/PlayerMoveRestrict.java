package com.profilesplus.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.profilesplus.RPGProfiles;
import com.profilesplus.SpectatorManager;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
                event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&aClick an available slot to create a profile!"));
            }
            else {
                spectatorManager.warn(event.getPlayer());
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (spectatorManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
            spectatorManager.warn(event.getPlayer());

        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (spectatorManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
        }

    }
    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event){
        if (spectatorManager.isWaiting(event.getPlayer())){
            event.setCancelled(true);
            spectatorManager.warn(event.getPlayer());

        }
    }
    @EventHandler
    public void onPlayer(PlayerDropItemEvent event){
        if (spectatorManager.isWaiting(event.getPlayer())){
            event.setCancelled(true);
            spectatorManager.warn(event.getPlayer());

        }
    }

    @EventHandler
    public void onPlayer(PlayerDeathEvent event){
        if (spectatorManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
            spectatorManager.warn(event.getPlayer());

        }
    }
    @EventHandler
    public void onPlayerA(PlayerJumpEvent event){
        if (spectatorManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
            spectatorManager.warn(event.getPlayer());
        }
    }
}