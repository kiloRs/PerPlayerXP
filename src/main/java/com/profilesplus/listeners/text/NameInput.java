package com.profilesplus.listeners.text;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.TextInputEvent;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.players.PlayerData;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
@Getter
public class NameInput implements Listener {
    private final Player player;
    private final PlayerData playerData;
    private final ProfileCreateMenu previousMenu;

    public NameInput(ProfileCreateMenu previousMenu) {
        this.player = previousMenu.getPlayerData().getPlayer();
        this.playerData = previousMenu.getPlayerData();
        this.previousMenu = previousMenu;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && event.getPlayer().hasMetadata("textInput")) {
            String input = event.signedMessage().message();
            if (!input.isEmpty()) {
                event.setCancelled(true);

                if (!isValidName(input)) {
                    player.sendMessage(ChatColor.RED + "Profile name too long. Maximum length is 16 characters.");
                } else {
                    Bukkit.getPluginManager().callEvent(new TextInputEvent(player,input));
                    RPGProfiles.log("Text Input to Selection of Name: " + input);
                }
            }
            AsyncChatEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTextInput(TextInputEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && event.getPlayer().hasMetadata("textInput")) {
            String input = event.getInput().toLowerCase();

            if (input.equals("close") || input.equals("cancel")) {
                // Return to the ProfileCreateMenu
                previousMenu.open();
            } else if (isValidName(input)) {
                // Process the name input here
                previousMenu.setProfileName(input);
                previousMenu.open();
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Invalid profile name. Please try again.");
            }

            // Unregister the listener when the input is processed or cancelled
            TextInputEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onText(TextInputEvent e){
        if (e.isCancelled()){
            previousMenu.setProfileName(null);
            previousMenu.open();
            return;
        }
        RPGProfiles.log("Name: " + e.getInput().toUpperCase());
    }

    private boolean isValidName(String name) {
        if (name.length() > 16 || !name.matches("^[a-zA-Z0-9]+$")) {
            return false;
        }

        return !previousMenu.isForbiddenName(name);
    }
}
