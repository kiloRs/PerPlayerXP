package com.profilesplus.events;

import com.profilesplus.players.Profile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfileRemoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Profile profile;
    @Setter
    private boolean cancelled;

    public ProfileRemoveEvent(Player player, Profile profile) {
        this.player = player;
        this.profile = profile;
    }

    public Player getPlayer() {
        return player;
    }

    public Profile getProfileName() {
        return profile;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
