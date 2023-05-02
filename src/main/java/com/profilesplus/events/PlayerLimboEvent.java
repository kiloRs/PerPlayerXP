package com.profilesplus.events;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerLimboEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final boolean enter;
    @Setter
    private boolean cancelled;
    @Setter
    private Location limboLocation = RPGProfiles.getLimboManager().getSpectatorLocation();

    public PlayerLimboEvent(boolean enter, Player player){
        this(enter,player,false);
    }
    public PlayerLimboEvent(boolean enter, Player player, boolean async){
        super(player,async);
        this.enter = enter;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
