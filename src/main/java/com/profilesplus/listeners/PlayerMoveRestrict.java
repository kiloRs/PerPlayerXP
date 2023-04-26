package com.profilesplus.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.profilesplus.LimboManager;
import com.profilesplus.menu.CharSelectionMenu;
import com.profilesplus.players.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveRestrict implements Listener {

    private final LimboManager limboManager;
    private double aDouble;

    public PlayerMoveRestrict(LimboManager limboManager) {
        this.limboManager = limboManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (limboManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);

        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (limboManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
        }

    }
    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event){
        if (limboManager.isWaiting(event.getPlayer())){
            event.setCancelled(true);
            limboManager.warn(event.getPlayer());

        }
    }
    @EventHandler
    public void onPlayer(PlayerDropItemEvent event){
        if (limboManager.isWaiting(event.getPlayer())){
            event.setCancelled(true);
            limboManager.warn(event.getPlayer());
            new CharSelectionMenu(PlayerData.get(event.getPlayer())).open();
        }
    }

    @EventHandler
    public void onPlayer(PlayerDeathEvent event){
        if (limboManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
            limboManager.warn(event.getPlayer());

        }
    }
    @EventHandler
    public void onPlayerA(PlayerJumpEvent event){
        if (limboManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
            limboManager.warn(event.getPlayer());
        }
    }
}