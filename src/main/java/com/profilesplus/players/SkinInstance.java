package com.profilesplus.players;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.profilesplus.RPGProfiles;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Getter
public class SkinInstance {
    private final UUID uuid;
    private final @NotNull(value = "Player is null!") Player player;
    private String url;

    public SkinInstance(UUID uuid){
        this.uuid = uuid;
        this.player = Validate.notNull(Bukkit.getPlayer(uuid),"Player cannot be null for Skins!");
    }

    public void changeSkinNow(){
        new BukkitRunnable(){
            @Override
            public void run() {
                changeSkin();
            }
        }.runTaskLater(RPGProfiles.getInstance(),1);
    }
    private void changeSkin(){
        PlayerProfile playerProfile = player.getPlayerProfile();
        PlayerTextures textures = playerProfile.getTextures();

        try {
            textures.setSkin(new URL(url), PlayerTextures.SkinModel.CLASSIC);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        playerProfile.setTextures(textures);
        player.setPlayerProfile(playerProfile);
        playerProfile.update().thenAcceptAsync(PlayerProfile::complete, runnable -> Bukkit.getScheduler().runTask(RPGProfiles.getInstance(), runnable));    }

    public void setUrl(String url) {
        this.url = url;
    }
}
