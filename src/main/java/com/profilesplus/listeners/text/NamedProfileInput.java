package com.profilesplus.listeners.text;

import com.profilesplus.RPGProfiles;
import io.lumine.mythic.lib.api.explorer.ChatInput;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Function;

public class NamedProfileInput extends ChatInput {
    public NamedProfileInput(Player player, Function<String, Boolean> output) {
        super(player, output);

        Bukkit.getPluginManager().registerEvents(this, RPGProfiles.getInstance());


    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void a(AsyncPlayerChatEvent event) {
        super.a(event);
    }

    @Override
    public void b(InventoryCloseEvent event) {
        super.b(event);
    }

    @Override
    public void c(InventoryOpenEvent event) {
        super.c(event);
    }
}
