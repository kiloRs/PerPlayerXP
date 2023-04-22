package com.profilesplus;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import lombok.Getter;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Getter
public class PlayerData {
    public static final File PLAYER_FOLDER = getPlayerDataFolder();
    private static final Map<UUID, PlayerData> playerDataInstances = new HashMap<>();

    private final UUID uuid;
    private final File playerFile;
    private final net.Indyuce.mmoitems.api.player.PlayerData mmoItems;
    private final MMOPlayerData mythicLib;
    private YamlConfiguration config;
    private final Map<String,Profile> profileMap = new HashMap<>();
    private final Player player;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCore;

    public PlayerData(Player player){
        this(player.getUniqueId());
    }
    private PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getOfflinePlayer(uuid).getPlayer();
        this.playerFile = new File(getPlayerDataFolder(), uuid + ".yml");
        this.mmoCore = net.Indyuce.mmocore.api.player.PlayerData.get(uuid);
        this.mmoItems = net.Indyuce.mmoitems.api.player.PlayerData.get(uuid);
        this.mythicLib = MMOPlayerData.get(uuid);
        loadConfig();
    }

    public static PlayerData get(UUID uuid) {
        return playerDataInstances.computeIfAbsent(uuid, k -> new PlayerData(uuid));
    }

    public static PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(playerFile);
    }

    private static File getPlayerDataFolder() {
        final File dataFolder = new File("plugins/ProfilesPlus/playerdata");
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }


}
