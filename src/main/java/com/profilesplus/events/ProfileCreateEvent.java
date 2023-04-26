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
    @Setter
    private boolean override;
    private Profile profile;
    private final PlayerData playerData;

    @Setter
    private boolean cancelled;
    @Setter
    private boolean activate;

    public ProfileCreateEvent(PlayerData p, Profile newProfile, boolean activate, boolean override) {
        this.override = override;
        this.player = p.getPlayer();
        this.playerData = p;
        this.profile = newProfile;
        this.cancelled = false;
        this.activate = activate;

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


