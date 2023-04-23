package com.profilesplus.listeners.text;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.TextInputEvent;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.players.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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
            if (!isValidName(input) || input.equalsIgnoreCase("close") || input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("back")){
                event.setCancelled(true);
                RPGProfiles.log("Text Input Event Cancelled");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = false)
    public void onText(TextInputEvent e){
        if (e.isCancelled()){
            previousMenu.setProfileName(null);
            previousMenu.open();
            TextInputEvent.getHandlerList().unregister(this);
            return;
        }
        if (e.getInput().equalsIgnoreCase("cancel")){
            previousMenu.open();
        }
        previousMenu.setProfileName(e.getInput());
        previousMenu.open();
        TextInputEvent.getHandlerList().unregister(this);
    }

    private boolean isValidName(String name) {
        return RPGProfiles.getNameConfigManager().canUse(player,name);
    }
}
