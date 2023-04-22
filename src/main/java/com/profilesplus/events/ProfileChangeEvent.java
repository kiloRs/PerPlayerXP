package com.profilesplus.events;

import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfileChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Profile oldProfile;
    private final PlayerData playerData;
    @Setter
    private Profile newProfile;
    private boolean cancelled;

    public ProfileChangeEvent(PlayerData playerData, Profile oldProfile, Profile newProfile) {
        this.player = playerData.getPlayer();
        this.playerData = playerData;
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
        this.cancelled = false;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
