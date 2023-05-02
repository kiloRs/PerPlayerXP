package com.profilesplus.events;

import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ProfileChangeEvent extends PlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Nullable
    private final Profile oldProfile;
    @Setter
    @NotNull(value = "Empty Profile Error")
    private Profile newProfile;
    private final boolean fresh;
    @Setter
    private boolean cancelled;

    public ProfileChangeEvent(PlayerData player, @Nullable Profile oldProfile, Profile newProfile, boolean fresh) {
        super(player.getPlayer());
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
        this.fresh = fresh;
        this.cancelled = false;
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
