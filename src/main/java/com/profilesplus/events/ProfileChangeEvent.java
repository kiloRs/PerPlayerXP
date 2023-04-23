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
import org.jetbrains.annotations.Nullable;

@Getter
public class ProfileChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    @Nullable
    private final Profile oldProfile;
    private final PlayerData playerData;
    @Setter
    @NotNull(value = "Empty Profile Error")
    private Profile newProfile;
    @Setter
    private boolean cancelled;

    public ProfileChangeEvent(PlayerData player,@Nullable Profile oldProfile, Profile newProfile) {
        this.player = player.getPlayer();
        this.playerData = player;
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean hasOldProfile() {
        return oldProfile != null;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
