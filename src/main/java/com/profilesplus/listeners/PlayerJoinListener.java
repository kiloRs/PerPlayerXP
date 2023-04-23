package com.profilesplus.listeners;


import com.profilesplus.RPGProfiles;
import com.profilesplus.SpectatorManager;
import com.profilesplus.events.ProfileCreateEvent;
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

        if (activeProfile == null || playerData.getProfiles().isEmpty()){
            playerData.getPlayer().sendMessage(MythicLib.plugin.parseColors("&cYou currently have no profiles!"));
            ProfilesMenu profilesMenu = new ProfilesMenu(RPGProfiles.getInstance(), playerData);
            profilesMenu.open();
        }
        else {
            activeProfile.update();
            event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&aYour profile was loaded!"));

            RPGProfiles.log("Activation of Profile: " + activeProfile.getId() + " : " + activeProfile.getIndex());
            return;


        }
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
