package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.ProfileChangeEvent;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
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
            event.getPlayerData().getProfileStorage().putProfile( event.getProfile());
        }
        else {
            if (!event.getPlayerData().getProfileStorage().hasProfile(event.getProfile().getIndex())) {
                event.getPlayerData().getProfileStorage().putProfile(event.getProfile());
            }
        }
        if (event.getProfile().isCreated()){
            RPGProfiles.debug("Profile exists in Map prior to Creation Event ERROR! SOMETHING IS WRONG!");
            return;
        }
        if (!event.getPlayer().getInventory().isEmpty()){
            event.getPlayer().getInventory().clear();
        }

        if (event.isActivate() || event.getPlayerData().getProfileStorage().getAll().size() == 1) {
            boolean fresh = true;
            if (!event.getPlayerData().changeProfile(event.getProfile(),fresh)){
                return;
            }
            if (event.getPlayerData().getProfileStorage().hasProfile(event.getProfile())) {
                event.getPlayer().sendMessage(RPGProfiles.getMessage(event.getPlayer(), "profile.create.successful", "&aProfile was created successfully!"));
            }


        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClass(PlayerChangeClassEvent event){
        if (PlayerData.get(event.getPlayer()) != null){
            event.setCancelled(true);
            event.getPlayer().sendMessage(RPGProfiles.getMessage("forbidden.mmocore.classes","&cClass Selection Via MMOCore is not allowed!"));
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileChange(ProfileChangeEvent event) {
        Player player = event.getPlayer();
        Profile oldProfile = event.getOldProfile();
        Profile newProfile = event.getNewProfile();

        if (event.getPlayerData().getProfileStorage().isActiveProfile(newProfile.getIndex())) {
            return;
        }
        if (!event.getPlayerData().getProfileStorage().hasProfile((newProfile.getIndex()))){
            event.setCancelled(true);
            RPGProfiles.log("Profile is not existing for player: " + player.getName() + " as " + newProfile.getIndex());
            return;
        }

        if (event.isCancelled()){
            return;
        }
        if (event.hasOldProfile()){
            assert oldProfile != null;
            PlayerData.get(event.getPlayer()).saveActiveProfile(true);
        }

        if (event.isFresh()) {
            if (RPGProfiles.getProfileConfigManager().hasCommands()) {
                for (String command : RPGProfiles.getProfileConfigManager().getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, command));
                }
                RPGProfiles.debug("Commands executed for Player: " + RPGProfiles.getProfileConfigManager().getCommands().size());
            }
            else {
                RPGProfiles.debug("No Commands for Initializing Profile Found!");
            }
        }

        if (RPGProfiles.getLimboManager().isWaiting(player)){
            RPGProfiles.getLimboManager().removeWaiting(player);
        }
        PlayerData.get(event.getPlayer()).saveActiveProfile(!event.isFresh());
    }

}
