package com.profilesplus.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.profilesplus.LimboManager;
import com.profilesplus.RPGProfiles;
import com.profilesplus.events.PlayerLimboEvent;
import com.profilesplus.menu.CharSelectionMenu;
import com.profilesplus.players.ActiveKeyHolder;
import com.profilesplus.players.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerMoveRestrict implements Listener {

    private final LimboManager limboManager;

    public PlayerMoveRestrict(LimboManager limboManager) {
        this.limboManager = limboManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (limboManager.isWaiting(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (limboManager.isWaiting(event.getPlayer())) {
            if (event.getTo().equals(limboManager.getSpectatorLocation().toBlockLocation())){
                return;
            }
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


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){

        if (new ActiveKeyHolder(e.getPlayer()).hasActive()){
            return;
        }
        RPGProfiles.getLimboManager().setWaiting(e.getPlayer(), e.getPlayer().getLocation().getWorld().getSpawnLocation().toBlockLocation());
    }

    @EventHandler
    public void onGameMode(PlayerGameModeChangeEvent event){
        if (event.getNewGameMode()== GameMode.SPECTATOR && RPGProfiles.getLimboManager().isWaiting(event.getPlayer())){
            RPGProfiles.debug("LIMBO -> Activate: " + event.getPlayer().getName());
            new CharSelectionMenu(PlayerData.get(event.getPlayer()));
        }


    }
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onMove(PlayerMoveEvent event){
//        if (RPGProfiles.getLimboManager().isWaiting(event.getPlayer())){
//            new CharSelectionMenu(PlayerData.get(event.getPlayer())).open();
//        }
//    }

    @EventHandler
    public void onLimbo(PlayerLimboEvent event){
        if (PlayerData.exists(event.getPlayer()) && PlayerData.get(event.getPlayer()).getKeyHolder().hasActive()){
            event.setCancelled(true);
            RPGProfiles.debug("Limbo was cancelled!");
        }
    }
}