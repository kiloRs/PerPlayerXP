package com.profilesplus.menu.text;

import com.profilesplus.menu.InputTextManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {
    private final InputTextManager inputTextManager;

    public ChatEventListener(InputTextManager inputTextManager) {
        this.inputTextManager = inputTextManager;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (inputTextManager.isInInputMode(player)) {
            event.setCancelled(true); // Cancel the event to prevent other players from seeing the input
            String message = event.getMessage();
            if ("cancel".equalsIgnoreCase(message)) {
                inputTextManager.stopInputMode(player);
            } else {
                inputTextManager.handleChatInput(player, message);
            }
        }
    }
}
