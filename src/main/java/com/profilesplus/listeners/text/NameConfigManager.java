package com.profilesplus.listeners.text;


import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public NameConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getMinNameLength() {
        return config.getInt("profile.name.minLength", 3);
    }

    public int getMaxNameLength() {
        return config.getInt("profile.name.maxLength", 16);
    }

    public boolean isLettersOnly() {
        return config.getBoolean("profile.name.lettersOnly", true);
    }

    private boolean isDuplicateName(String name) {
        return PlayerData.getPlayerDataInstances().values().stream().anyMatch(playerData -> playerData.getProfiles().values().stream().anyMatch(profile -> profile.getId().equalsIgnoreCase(name)));
    }

    public boolean canUse(Player player, String name){
        if (isForbiddenName(name)){
            if (RPGProfiles.isLogging()) {
                RPGProfiles.log(player.getName() + " attempting to register name: " + name + " is forbidden!");
            }
            return false;
        }
        if (isDuplicateName(name)){
            if (RPGProfiles.isLogging()) {
                RPGProfiles.log(player.getName() + " attempting to register a duplicate name : " + name);
            }return false;
        }
        return isValidName(name);
    }
    private boolean isValidName(String name) {
        int minLength = getMinNameLength();
        int maxLength = getMaxNameLength();
        boolean lettersOnly = isLettersOnly();

        if (name.length() < minLength || name.length() > maxLength) {
            return false;
        }

        if (lettersOnly && !name.matches("^[a-zA-Z]+$")) {
            return false;
        }

        return !isForbiddenName(name) && !isDuplicateName(name);
    }
    public List<String> getForbiddenNames() {
        return config.getStringList("profile.name.forbidden");
    }
    private boolean isForbiddenName(String name) {
        List<String> forbiddenNames =this.getForbiddenNames();
        for (String forbiddenName : forbiddenNames) {
            Pattern pattern = Pattern.compile(forbiddenName, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

}
