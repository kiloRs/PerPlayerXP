package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import com.profilesplus.configs.SavedInformation;
import lombok.Getter;
import lombok.Setter;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.player.DefaultPlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class SavedProfile {
    private final ConfigurationSection section;
    private final int index;
    private final String id;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCore;
    @Setter
    private PlayerClass playerClass;
    private final Player player;
    private SavedInformation savedInformation = null;
    private final PlayerData internalPlayer;
    public SavedProfile(int index, String id, PlayerClass playerClass, Player player){
        this.index = index;
        this.id = id;
        this.playerClass = playerClass;
        this.player = player;
        this.internalPlayer = PlayerData.get(player);
        this.section = internalPlayer.getConfig().isConfigurationSection("profiles." + this.index)? internalPlayer.getConfig().getConfigurationSection("profiles." + this.index):null;
        mmoCore = net.Indyuce.mmocore.api.player.PlayerData.get(player);
        if (this.section == null) {
            if (RPGProfiles.getStatHandler().has(playerClass.getId())) {
                this.savedInformation = RPGProfiles.getStatHandler().get(playerClass.getId());
            }
            else {
                this.savedInformation = new SavedInformation(new SavedInformation(new DefaultPlayerData(1,0,0,0,0,0,0,20,0,0,0)));
            }
        }
    }

}
