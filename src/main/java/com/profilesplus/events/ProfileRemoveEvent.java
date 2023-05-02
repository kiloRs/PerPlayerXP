package com.profilesplus.events;

import com.profilesplus.players.Profile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfileRemoveEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Profile profile;
    @Setter
    private boolean cancelled;

    public ProfileRemoveEvent(Profile profile) {
        super(profile.getInternalPlayer().getPlayer());
        this.profile = profile;
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
