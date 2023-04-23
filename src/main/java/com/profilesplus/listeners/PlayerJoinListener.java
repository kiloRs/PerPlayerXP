package com.profilesplus.listeners;


import com.profilesplus.RPGProfiles;
import com.profilesplus.SpectatorManager;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.menu.InventoryGUI;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.AsyncPlayerDataLoadEvent;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        SpectatorManager manager = ((RPGProfiles) RPGProfiles.getInstance()).getSpectatorManager();

        manager.setWaiting(player);
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(AsyncPlayerDataLoadEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        Profile activeProfile = playerData.getActiveProfile();

        if (activeProfile == null && playerData.getProfileMap().isEmpty()) {
            // Open the CreateProfileMenu
            ProfileCreateMenu menu = new ProfileCreateMenu(playerData,null);
            menu.open();

            event.getPlayer().sendMessage((MythicLib.plugin.parseColors("&eOpening Profile Create Menu from Async Event")));

            // Set the player to spectator mode and disable movement

            RPGProfiles.log("No Profiles Locate for Player... " + playerData.getPlayer().getName());

            // Use ProtocolLib or other methods to restrict movement
        }
        else if (!playerData.getProfileMap().isEmpty()){
            ProfilesMenu menu = new ProfilesMenu(RPGProfiles.getInstance(),playerData);
            menu.open();
            event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&eOpening Profiles Menu from Async Event"));

        }
        else {
            activeProfile.update();

            event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&aProfile assigning from async event: " + activeProfile.getId()));

            RPGProfiles.log("Activation of Profile: " + activeProfile.getId() + " : " + activeProfile.getIndex());
            return;


        }
    }
    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (event.getReason() != InventoryCloseEvent.Reason.PLUGIN || event.getReason() != InventoryCloseEvent.Reason.UNKNOWN){
            RPGProfiles.log("Close - " + event.getReason());
            return;
        }

        SpectatorManager manager = ((RPGProfiles) RPGProfiles.getInstance()).getSpectatorManager();

        if (event.getInventory().getHolder() instanceof ProfilesMenu profilesMenu){
            if (manager.isWaiting((Player) event.getPlayer())){
                event.getPlayer().sendMessage("You are waiting!");
                return;
            }
            RPGProfiles.log("ProfilesMenu waiting...");
            return;
        }
        if ((event.getInventory().getHolder() instanceof ProfileCreateMenu createMenu)) {
            if (manager.isWaiting((Player) event.getPlayer())){
                event.getPlayer().sendMessage("You are waiting!");
                return;
            }
            RPGProfiles.log("ProfileCreateMenu waiting...");
            return;
        }

        if (event.getInventory().getHolder() instanceof InventoryGUI inventoryGUI){
            return;
        }
        RPGProfiles.log("Close Menu - Non InventoryGUI");
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onProfileCreate(ProfileCreateEvent event) {
        Player player = event.getPlayer();
        String profileName = event.getProfile().getName();
        String profileClass = event.getProfile().getClassName();
        int activeSlot = event.getProfile().getIndex();
        int bukkitSlot = ProfilesMenu.profileSlotToInventorySlot(activeSlot)>=0?ProfilesMenu.profileSlotToInventorySlot(activeSlot):-1;

        if (RPGProfiles.isLogging()){
            RPGProfiles.log("Profile Being Created - " + activeSlot + " " + profileName + " " + profileClass.toUpperCase() + " on slot " + bukkitSlot);
        }
        // Perform your own actions or checks here

        PlayerClass aClass = MMOCore.plugin.classManager.get(profileClass);
        if (aClass == null){
            event.setCancelled(true);
            player.sendMessage("Invalid class name!");
            return;
        }
        // Example: Cancel the event if the profile name is in the list of forbidden names
        List<String> forbiddenNames = ((RPGProfiles) RPGProfiles.getInstance()).getForbiddenNames();
        for (String forbiddenName : forbiddenNames) {
            if (profileName.equalsIgnoreCase(forbiddenName)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use '" + forbiddenName + "' in your profile name.");
                break;
            }
        }
    }
    public static Location getSpectatorLocation() {
        return ((RPGProfiles) RPGProfiles.getInstance()).getSpectatorManager().getSpectatorLocation();
    }
}
