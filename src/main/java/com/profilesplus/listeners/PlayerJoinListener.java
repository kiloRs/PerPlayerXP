package com.profilesplus.listeners;


import com.profilesplus.ProfilesPlus;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        Profile activeProfile = playerData.getActiveProfile();

        if (activeProfile == null && playerData.getProfileMap().isEmpty()) {
            // Open the CreateProfileMenu
            ProfileCreateMenu menu = new ProfileCreateMenu(playerData,null);
            menu.open();


            // Set the player to spectator mode and disable movement

            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            event.getPlayer().teleport(((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager().getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            event.getPlayer().setFlySpeed(0);

            ProfilesPlus.log("No Profiles Locate for Player... " + playerData.getPlayer().getName());

            // Use ProtocolLib or other methods to restrict movement
        }
        else if (!playerData.getProfileMap().isEmpty()){
            ProfilesMenu menu = new ProfilesMenu(ProfilesPlus.getInstance(),playerData);
            menu.open();

            // Set the player to spectator mode and disable movement
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            event.getPlayer().teleport(((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager().getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            event.getPlayer().setFlySpeed(0);
            ProfilesPlus.log("No Profiles Locate for Player: " + playerData.getPlayer().getName());
            // Use ProtocolLib or other methods to restrict movement
        }
        else {
            activeProfile.update();
            ProfilesPlus.log("Activation of Profile: " + activeProfile.getId() + " : " + activeProfile.getIndex());
            return;


        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Replace 'Create Profile' with the title of the inventory you want to trigger this behavior
        PlayerData p = PlayerData.get(player);
        if (p.getActiveProfile() != null){
            return;
        }

        if ((event.getInventory().getHolder() instanceof ProfileCreateMenu createMenu && p.getActiveProfile() == null)) {
            Location spectatorLocation = getSpectatorLocation();
            player.teleport(spectatorLocation);
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlySpeed(0);

            ProfilesPlus.log("Teleport via InventoryCloseEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onProfileCreate(ProfileCreateEvent event) {
        Player player = event.getPlayer();
        String profileName = event.getProfile().getName();
        String profileClass = event.getProfile().getClassName();
        int activeSlot = event.getProfile().getIndex();
        int bukkitSlot = ProfilesMenu.profileSlotToInventorySlot(activeSlot)>=0?ProfilesMenu.profileSlotToInventorySlot(activeSlot):-1;

        if (ProfilesPlus.isLogging()){
            ProfilesPlus.log("Profile Being Created - " + activeSlot + " " + profileName + " " + profileClass.toUpperCase() + " on slot " + bukkitSlot);
        }
        // Perform your own actions or checks here

        PlayerClass aClass = MMOCore.plugin.classManager.get(profileClass);
        if (aClass == null){
            event.setCancelled(true);
            player.sendMessage("Invalid class name!");
            return;
        }
        // Example: Cancel the event if the profile name is in the list of forbidden names
        List<String> forbiddenNames = ((ProfilesPlus) ProfilesPlus.getInstance()).getForbiddenNames();
        for (String forbiddenName : forbiddenNames) {
            if (profileName.equalsIgnoreCase(forbiddenName)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use '" + forbiddenName + "' in your profile name.");
                break;
            }
        }
    }
    public static Location getSpectatorLocation() {
        Plugin plugin = ProfilesPlus.getInstance();
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
