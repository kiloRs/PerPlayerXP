package com.profilesplus.menu.text;

import com.profilesplus.ProfilesPlus;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginEvent;

public class ChatEventListener implements Listener {
    private final InputTextManager inputTextManager;

    public ChatEventListener(InputTextManager inputTextManager) {
        this.inputTextManager = inputTextManager;
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onJoin(PluginEnableEvent event){
        if (event.getPlugin()== ProfilesPlus.getInstance()){
            ProfilesPlus.log("Initializing ProfilePlus Plugin!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (inputTextManager.isInInputMode(player)) {
            event.setCancelled(true); // Cancel the event to prevent other players from seeing the input
            String message = event.signedMessage().message();

            if ("cancel".equalsIgnoreCase(message)) {
                inputTextManager.stopInputMode(player);
            } else {
                inputTextManager.handleChatInput(player, message);
            }
        }
    }
}
