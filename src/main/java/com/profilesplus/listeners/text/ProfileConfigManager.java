package com.profilesplus.listeners.text;


import com.profilesplus.RPGProfiles;
import com.profilesplus.players.DefaultData;
import com.profilesplus.players.PlayerData;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final List<DefaultData> alldata = new ArrayList<>();

    public ProfileConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        loadAll();
    }

    public void loadAll(){
        for (String key : RPGProfiles.getDefaultPlayerConfig().getKeys(false)) {
            if (RPGProfiles.getDefaultPlayerConfig().isConfigurationSection(key) && !RPGProfiles.getDefaultPlayerConfig().getConfigurationSection(key).getKeys(false).isEmpty()){
                DefaultData e = new DefaultData(key);
                alldata.add(e);

                if (!e.exists()){
                    alldata.remove(e);
                }
            }
        }
    }

    public int getMinNameLength() {
        return config.getInt("profile.naming.minLength", 3);
    }

    public int getMaxNameLength() {
        return config.getInt("profile.naming.maxLength", 16);
    }

    public boolean isLettersOnly() {
        return config.getBoolean("profile.naming.lettersOnly", true);
    }

    public boolean isDuplicateName(String name) {
        return PlayerData.getPlayerDataInstances().values().stream().anyMatch(playerData -> playerData.getProfiles().values().stream().anyMatch(profile -> profile.getId().equalsIgnoreCase(name)));
    }

    public boolean canUse(Player player, String string){
        return canUse(player,string,true);
    }

    public boolean canUse(Player player, String name, boolean alert){
        if (isForbiddenName(name)){
            if (alert) {
                if (RPGProfiles.isLogging()) {
                    RPGProfiles.log(player.getName() + " attempting to register name: " + name + " is forbidden!");
                }
                player.sendMessage(RPGProfiles.getMessage(player,"naming.forbidden", "&cThis name is not allowed!"));
            }return false;
        }
        if (isDuplicateName(name)){
            if (alert) {
                if (RPGProfiles.isLogging()) {
                    RPGProfiles.log(player.getName() + " attempting to register a duplicate name : " + name);

                }
                player.sendMessage(RPGProfiles.getMessage(player,"naming.duplicate", "&cThis name has already been used!"));
            }
            return false;
        }
        if (!isValidName(name)){
            if (alert) {
                player.sendMessage(RPGProfiles.getMessage(player,"naming.invalid", "&cThe name you entered was invalid " + (isLettersOnly() ? "Names must be letters only and " : "Max length: " + getMaxNameLength() + " Min Length: " + getMinNameLength())));
            }return false;
        }
        return true;
    }
    public boolean isValidName(String name) {
        int minLength = getMinNameLength();
        int maxLength = getMaxNameLength();
        boolean lettersOnly = isLettersOnly();

        if (name.length() < minLength || name.length() > maxLength) {
            return false;
        }

        return !lettersOnly || name.matches("^[a-zA-Z]+$");
    }
    public List<String> getForbiddenNames() {
        return config.getStringList("profile.naming.forbidden");
    }
    public boolean isForbiddenName(String name) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ProfileConfigManager that)) return false;

        return new EqualsBuilder().append(plugin, that.plugin).isEquals();
    }


    public boolean hasCommands(){
        return !getCommands().isEmpty();
    }
    public List<String> getCommands(){
        return RPGProfiles.getInstance().getConfig().isList("profile.create.commands")?RPGProfiles.getInstance().getConfig().getStringList("profile.create.commands"):new ArrayList<>();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(plugin).toHashCode();
    }
}
