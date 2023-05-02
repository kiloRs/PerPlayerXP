package com.profilesplus.listeners.text;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.TextInputEvent;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.players.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public class NameInput implements Listener {
    private final Player player;
    private final PlayerData playerData;
    private final ProfileCreateMenu previousMenu;

    public NameInput(@NotNull(value = "Profile Create Menu is Null!") ProfileCreateMenu previousMenu) {
        this.player = previousMenu.getPlayerData().getPlayer();
        this.playerData = previousMenu.getPlayerData();
        this.previousMenu = previousMenu;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && event.getPlayer().hasMetadata("textInput") || previousMenu != null && previousMenu.isInput()) {
            String input = event.signedMessage().message();
            if (previousMenu == null || !previousMenu.isInput()){
                return;
            }
            if (!input.isEmpty()) {
                event.setCancelled(true);

                if (!isValidName(input)) {
                    return;
                } else {
                            TextInputEvent inputEvent = new TextInputEvent(player, input);
                            Bukkit.getPluginManager().callEvent(inputEvent);

                            if (inputEvent.isCancelled()){
                                previousMenu.open();
                                inputEvent.getPlayer().removeMetadata("textInput",RPGProfiles.getInstance());
                                inputEvent.getPlayer().sendMessage(MythicLib.plugin.parseColors("&ePlease use another name! Not allowing: " + input.toUpperCase()));
                                AsyncChatEvent.getHandlerList().unregister(NameInput.this);
                                return;
                            }
                            inputEvent.getPlayer().removeMetadata("textInput",RPGProfiles.getInstance());
                    }
                }
            }
            else {
                event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&eYou must enter a name of the profile, or type cancel!"));
                return;
            }
            AsyncChatEvent.getHandlerList().unregister(this);
        }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTextInput(TextInputEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && event.getPlayer().hasMetadata("textInput")) {
            String input = event.getText().toLowerCase();

            // Add more words to the list as needed
            List<String> cancelWords = Arrays.asList("cancel", "close", "end", "stop", "back", "exit");
            if (RPGProfiles.getInstance().getConfig().isList("profile.naming.cancel")){
                cancelWords = RPGProfiles.getInstance().getConfig().getStringList("profile.naming.cancel");

            }
            if (cancelWords.contains(input.toLowerCase())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = false)
    public void onText(TextInputEvent e){
        if (e.isCancelled()){
            previousMenu.open();
            TextInputEvent.getHandlerList().unregister(this);

            return;
        }
        if (!isValidName(e.getText())){
            return;
        }
        previousMenu.setProfileName(e.getText());
        previousMenu.open();
//        TextInputEvent.getHandlerList().unregister(this);
        TextInputEvent.getHandlerList().unregister(this);

    }

    private boolean isValidName(String name) {
        return RPGProfiles.getProfileConfigManager().canUse(player,name);
    }
}
