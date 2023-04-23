package com.profilesplus.menu.text;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.events.TextInputEvent;
import com.profilesplus.players.PlayerData;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
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

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.getPlayer().equals(player)) {
            String input = event.signedMessage().message();
            if (!input.isEmpty()) {
                event.setCancelled(true);

                if (!isValidName(input)) {
                    player.sendMessage(ChatColor.RED + "Profile name too long. Maximum length is 16 characters.");
                } else {
                    previousMenu.setProfileName(input);
                    player.sendMessage(ChatColor.GREEN + "Profile name set to: " + input);
                    previousMenu.open();
                }
            }
            AsyncChatEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler
    public void onTextInput(TextInputEvent event) {
        if (event.getPlayer().equals(player)) {
            String input = event.getInput().toLowerCase();

            if (input.equals("close") || input.equals("cancel")) {
                // Return to the ProfileCreateMenu
                previousMenu.open();
            } else if (isValidName(input)) {
                // Process the name input here
                previousMenu.setProfileName(input);
                previousMenu.open();
            } else {
                player.sendMessage(ChatColor.RED + "Invalid profile name. Please try again.");
            }

            // Unregister the listener when the input is processed or cancelled
            TextInputEvent.getHandlerList().unregister(this);
        }
    }

    private boolean isValidName(String name) {
        if (name.length() > 16 || !name.matches("^[a-zA-Z0-9]+$")) {
            return false;
        }

        List<String> forbiddenNames = ((ProfilesPlus) ProfilesPlus.getInstance()).getForbiddenNames();
        for (String forbiddenName : forbiddenNames) {
            if (name.equalsIgnoreCase(forbiddenName)) {
                return false;
            }
        }

        return true;
    }
}
