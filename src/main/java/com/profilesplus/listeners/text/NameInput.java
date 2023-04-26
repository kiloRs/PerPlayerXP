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
import org.bukkit.scheduler.BukkitRunnable;
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

    @EventHandler(priority = EventPriority.HIGHEST)
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
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            TextInputEvent event = new TextInputEvent(player, input);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()){
                                previousMenu.open();
                                event.getPlayer().removeMetadata("textInput",RPGProfiles.getInstance());
                                event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&ePlease use another name! Not allowing: " + input.toUpperCase()));
                                AsyncChatEvent.getHandlerList().unregister(NameInput.this);
                                return;
                            }
                            previousMenu.setProfileName(input);
                            previousMenu.open();
                            event.getPlayer().removeMetadata("textInput",RPGProfiles.getInstance());


                        }
                    }.runTask(RPGProfiles.getInstance());
                }
            }
            else {
                event.getPlayer().sendMessage(MythicLib.plugin.parseColors("&eYou must enter a name of the profile, or type cancel!"));
                return;
            }
            AsyncChatEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTextInput(TextInputEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && event.getPlayer().hasMetadata("textInput")) {
            String input = event.getInput().toLowerCase();

            if (RPGProfiles.isLogging()){
                RPGProfiles.log("Text Input Event: " + event.getInput().toUpperCase());
            }
            // Add more words to the list as needed
            List<String> cancelWords = Arrays.asList("cancel", "close", "end", "stop", "back", "exit");
            if (RPGProfiles.getInstance().getConfig().isList("naming.stopWords")){
                cancelWords = RPGProfiles.getInstance().getConfig().getStringList("naming.stopWords");
            }
            if (cancelWords.contains(input.toLowerCase())) {
                event.setCancelled(true);
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
        if (!isValidName(e.getInput())){
            return;
        }
        previousMenu.setProfileName(e.getInput());
        previousMenu.open();
        TextInputEvent.getHandlerList().unregister(this);
    }

    private boolean isValidName(String name) {
        return RPGProfiles.getProfileConfigManager().canUse(player,name);
    }
}
