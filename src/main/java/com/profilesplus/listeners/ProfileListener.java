package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.ProfileChangeEvent;
import com.profilesplus.players.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProfileListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileChange(ProfileChangeEvent event) {
        Player player = event.getPlayer();
        Profile oldProfile = event.getOldProfile();
        Profile newProfile = event.getNewProfile();

        if (event.getPlayerData().isActive(newProfile.getIndex())) {
            throw new RuntimeException("Cannot Switch to Active Profile!");
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
            if (RPGProfiles.isLogging()){
                RPGProfiles.log("Old: " + oldProfile.getId() + " " + oldProfile.getIndex());
            }
        }
        if (RPGProfiles.isLogging()){
            RPGProfiles.log("New: " + newProfile.getId() + " " + newProfile.getIndex());
        }
    }

}
