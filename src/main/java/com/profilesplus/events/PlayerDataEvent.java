package com.profilesplus.events;

import com.profilesplus.players.PlayerData;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class PlayerDataEvent extends PlayerEvent implements Cancellable {
    private final PlayerData playerData;
    public PlayerDataEvent(@NotNull Player player){
        this(PlayerData.get(player));
    }
    public PlayerDataEvent(@NotNull PlayerData playerData) {
        super(playerData.getPlayer());
        this.playerData = playerData;
    }

    public PlayerDataEvent(@NotNull PlayerData playerData, boolean async) {
        super(playerData.getPlayer(), async);
        this.playerData = playerData;
    }
}
