package com.profilesplus;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.jeff_media.morepersistentdatatypes.DataType.LOCATION;
import static com.profilesplus.RPGProfiles.getMessage;

public class LimboManager {
    private final Set<UUID> spectatorWaitingModePlayers = new HashSet<>();
    private final RPGProfiles plugin;
    private final NamespacedKey locKey = new NamespacedKey(RPGProfiles.getInstance(), "originalLocation");

    public LimboManager(RPGProfiles plugin){
        this.plugin = plugin;
    }

    public boolean isWaiting(Player player){
        return spectatorWaitingModePlayers.contains(player.getUniqueId())||(player.hasMetadata("waiting") && player.getMetadata("waiting").get(0).asBoolean());
    }
    public void setWaiting(Player p, Location originalLocation){
        if (originalLocation != null){
            p.getPersistentDataContainer().set(locKey, LOCATION,originalLocation.toBlockLocation());
            p.saveData();
        }
        waitFor(p);
        spectatorWaitingModePlayers.add(p.getUniqueId());
    }
    public void removeWaiting(Player p){
        if (isWaiting(p)){
            p.setGameMode(GameMode.SURVIVAL);
            p.setFlySpeed(0.2f);
            p.removeMetadata("waiting", RPGProfiles.getInstance());
            spectatorWaitingModePlayers.remove(p.getUniqueId());

            teleportToOriginalLocation(p);
            p.getPersistentDataContainer().remove(locKey);
            p.saveData();
            }
    }

    private void waitFor(Player player){
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setFlySpeed(0);
        player.setMetadata("waiting",new FixedMetadataValue(RPGProfiles.getInstance(),true));
    }
    public Location getSpectatorLocation() {
        String worldName = plugin.getConfig().getString("limbo.location.world");
        if (worldName == null){
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        World world = Bukkit.getWorld(worldName);
        double x = plugin.getConfig().getDouble("limbo.location.x");
        double y = plugin.getConfig().getDouble("limbo.location.y");
        double z = plugin.getConfig().getDouble("limbo.location.z");
        double pitch =  plugin.getConfig().getDouble("limbo.location.pitch",0f);
        double yaw = plugin.getConfig().getDouble("limbo.location.yaw",0f);

        return new Location(world, x, y, z, ((float) yaw), ((float) pitch));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof LimboManager that)) return false;

        return new EqualsBuilder().append(plugin, that.plugin).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(plugin).toHashCode();
    }

    public void warn(Player player) {
        if (usesWarning()) {
            player.sendMessage(getMessage(player, "limbo.warn", "&eYou must create or select a profile to play here!"));
        }
        if (!player.getLocation().toBlockLocation().equals(getSpectatorLocation().toBlockLocation())) {
            player.teleport(getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    private boolean usesWarning() {
        if (RPGProfiles.getInstance().getConfig().isConfigurationSection("limbo.warning")) {
            if (!RPGProfiles.getInstance().getConfig().isBoolean("limbo.warning.enabled")){
                RPGProfiles.getInstance().getConfig().set("limbo.warning.enabled",true);
                RPGProfiles.getInstance().saveConfig();
            }
            return RPGProfiles.getInstance().getConfig().getBoolean("limbo.warning.enabled", true);
        }
        return true;
    }
    public boolean hasOriginalLocation(Player player){
        return player.getPersistentDataContainer().has(locKey, LOCATION) && player.getPersistentDataContainer().get(locKey,LOCATION) != null;
    }
    public Location getOriginalLocation(Player player){
        return player.getPersistentDataContainer().getOrDefault(locKey, LOCATION,player.getWorld().getSpawnLocation());
    }
    public void teleportToOriginalLocation(Player player){
        if (hasOriginalLocation(player)){
            player.teleport(getOriginalLocation(player), PlayerTeleportEvent.TeleportCause.PLUGIN);
            return;
        }
        if (getOriginalLocation(player).toBlockLocation().equals(player.getLocation().getWorld().getSpawnLocation().toBlockLocation())){
            player.teleport(getOriginalLocation(player), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

    }
}
