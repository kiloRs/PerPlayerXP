package com.profilesplus.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TextInputEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String input;

    public TextInputEvent(Player player, String input) {
        this.player = player;
        this.input = input;
    }

    public Player getPlayer() {
        return player;
    }

    public String getInput() {
        return input;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
