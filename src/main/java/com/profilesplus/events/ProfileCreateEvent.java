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
public class ProfileCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private Profile profile;
    private final PlayerData playerData;

    @Setter
    private boolean cancelled;

    public ProfileCreateEvent(PlayerData playerData, Profile profile) {
        this.player = playerData.getPlayer();
        this.playerData = playerData;
        this.profile = profile;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setName(String i){
        profile = new Profile(i, profile.getClassName(),profile.getIndex(), player);

    }
    public void setClassType(String i){
        profile = new Profile(getProfile().getId(),i,profile.getIndex(),player);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}


