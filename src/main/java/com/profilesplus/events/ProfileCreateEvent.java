package com.profilesplus.events;

import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfileCreateEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Setter
    private boolean override;
    private Profile profile;

    @Setter
    private boolean cancelled;
    @Setter
    private boolean activate;

    public ProfileCreateEvent(PlayerData p, Profile newProfile, boolean activate, boolean override) {
        super(p.getPlayer());
        this.override = override;
        this.player = p.getPlayer();
        this.profile = newProfile;
        this.cancelled = false;
        this.activate = activate;

    }

    public Profile getProfile() {
        return profile;
    }

    public void setName(String i){
        profile = new Profile(i, profile.getClassName(),profile.getIndex(), player.getUniqueId(),false);

    }
    public void setClassType(String i){
        profile = new Profile(getProfile().getId(),i,profile.getIndex(),player.getUniqueId(),false);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}


