package com.profilesplus.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
@Getter
public class TextInputEvent extends PlayerEvent implements Cancellable {
    @Getter
    private static HandlerList handlerList = new HandlerList();
    @Setter
    private final String text;
    @Setter
    private boolean cancelled = false;

    public TextInputEvent(Player player, String text){
        super(player);
        this.text = text;

    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }


}
