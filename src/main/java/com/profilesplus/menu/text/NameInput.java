package com.profilesplus.menu.text.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class NameInput implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String input = event.signedMessage().message();

        // Call the TextInputEvent with the player and input
        TextInputEvent textInputEvent = new TextInputEvent(player, input);
        Bukkit.getPluginManager().callEvent(textInputEvent);

        // You can cancel the chat event if you want to prevent the message from being sent to the chat
        event.setCancelled(true);
    }
}
