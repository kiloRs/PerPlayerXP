package com.profilesplus;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpectatorManager {
    private static final Set<UUID> spectatorWaitingModePlayers = new HashSet<>();
    private final RPGProfiles plugin;

    public SpectatorManager(RPGProfiles plugin){
        this.plugin = plugin;
    }

    public boolean isWaiting(Player player){
        return spectatorWaitingModePlayers.contains(player.getUniqueId())||(player.hasMetadata("waiting") && player.getMetadata("waiting").get(0).asBoolean());
    }
    public void setWaiting(Player p){
        waitFor(p);
        spectatorWaitingModePlayers.add(p.getUniqueId());
    }
    public void removeWaiting(Player p){
        if (isWaiting(p)){
            p.setGameMode(GameMode.SURVIVAL);
            p.setFlySpeed(0.2f);
            p.removeMetadata("waiting", RPGProfiles.getInstance());
            spectatorWaitingModePlayers.remove(p.getUniqueId());
        }
    }

    private void waitFor(Player player){
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setFlySpeed(0);
        player.setMetadata("waiting",new FixedMetadataValue(RPGProfiles.getInstance(),true));
    }
    public Location getSpectatorLocation() {
        String worldName = plugin.getConfig().getString("spectator-location.world");
        if (worldName == null){
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        World world = Bukkit.getWorld(worldName);
        double x = plugin.getConfig().getDouble("spectator-location.x");
        double y = plugin.getConfig().getDouble("spectator-location.y");
        double z = plugin.getConfig().getDouble("spectator-location.z");
        double pitch =  plugin.getConfig().getDouble("spectator-location.pitch",0f);
        double yaw = plugin.getConfig().getDouble("spectator-location.yaw",0f);

        return new Location(world, x, y, z, ((float) yaw), ((float) pitch));
    }
}
