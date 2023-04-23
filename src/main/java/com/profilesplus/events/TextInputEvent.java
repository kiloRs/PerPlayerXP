package com.profilesplus.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TextInputEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String input;
    @Getter
    @Setter
    private boolean cancelled = false;

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
