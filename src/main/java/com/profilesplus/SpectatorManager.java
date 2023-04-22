package com.profilesplus;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpectatorManager {
    private static final Set<UUID> spectatorWaitingModePlayers = new HashSet<>();
    private final ProfilesPlus plugin;

    public SpectatorManager(ProfilesPlus plugin){
        this.plugin = plugin;
    }

    public boolean isWaiting(Player player){
        return spectatorWaitingModePlayers.contains(player.getUniqueId());
    }
    public void setWaiting(Player p){
        spectatorWaitingModePlayers.add(p.getUniqueId());
    }
    public void removeWaiting(Player p){
        spectatorWaitingModePlayers.remove(p.getUniqueId());
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
        float pitch = (float) plugin.getConfig().get("spectator-location.pitch",0);
        float yaw = ((float) plugin.getConfig().get("spectator-location.yaw",0));

        return new Location(world, x, y, z,yaw,pitch);
    }
}
