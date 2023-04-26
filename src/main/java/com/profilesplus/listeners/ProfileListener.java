package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.ProfileChangeEvent;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.players.Profile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProfileListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProfileCreateNORMAL(ProfileCreateEvent event){
        if (event.isActivate()){
            RPGProfiles.debug("Profile being created is going to activate!");
        }
        else {
            RPGProfiles.debug("Profile being created will not activate!");
        }
    }
    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onProfileCreate(ProfileCreateEvent event){
        if (event.isOverride()) {
            event.getPlayerData().getProfileMap().put(event.getProfile().getIndex(), event.getProfile());
        }
        else {
            event.getPlayerData().getProfileMap().putIfAbsent(event.getProfile().getIndex(),event.getProfile());
        }

        if (event.isActivate() || event.getPlayerData().getProfiles().size() == 1) {
            String perm = event.getPerm();

            if (!event.getProfile().hasSaveInConfig()) {
                if (!event.getPlayerData().changeProfile(event.getProfile(), perm)) {
                    RPGProfiles.debug("Change Profile Failure!");
                }

                RPGProfiles.debug("Profile Successfully Changed and Registered! [" + event.getProfile().getName() + "]");

                if (RPGProfiles.getLimboManager().isWaiting(event.getPlayer())) {
                    event.getPlayerData().setLimbo(false);

                }
            }

            if (event.getPlayerData().isActive(event.getProfile().getIndex())) {
                event.getPlayer().sendMessage("Profile Active: " + event.getProfile().getIndex());
            }
            if (event.getPlayerData().getProfiles().containsValue(event.getProfile())) {
                event.getPlayer().sendMessage(RPGProfiles.getMessage(event.getPlayer(), "profile.create.successful", "&aProfile was created successfully!"));
            }


        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileChange(ProfileChangeEvent event) {
        Player player = event.getPlayer();
        Profile oldProfile = event.getOldProfile();
        Profile newProfile = event.getNewProfile();

        if (event.getPlayerData().isActive(newProfile.getIndex())) {
            return;
        }
        if (!event.getPlayerData().getProfiles().containsKey(newProfile.getIndex())){
            event.setCancelled(true);
            RPGProfiles.log("Profile is not existing for player: " + player.getName() + " as " + newProfile.getIndex());
            return;
        }

        if (event.isCancelled()){
            return;
        }

        if (RPGProfiles.isLogging()){
            RPGProfiles.log("Assigning New Profile for " + player.getName());
        }
        if (event.hasOldProfile()){
            assert oldProfile != null;
            if (RPGProfiles.isLogging()){
                RPGProfiles.log("Old: " + oldProfile.getId() + " " + oldProfile.getIndex());
            }
            oldProfile.saveToConfigurationSection(true);
        }
        if (RPGProfiles.isLogging()){
            RPGProfiles.log("New: " + newProfile.getId() + " " + newProfile.getIndex());
        }

        newProfile.saveToConfigurationSection(false);

        if (RPGProfiles.getProfileConfigManager().hasCommands()) {
            for (String command : RPGProfiles.getProfileConfigManager().getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, command));
            }
            RPGProfiles.debug("Commands executed for Player: " + RPGProfiles.getProfileConfigManager().getCommands().size());
            return;
        }
        RPGProfiles.debug("NO Commands were found in Config.yml to execute from Profile!");
    }

}
