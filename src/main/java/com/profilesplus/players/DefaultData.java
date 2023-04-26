package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public class DefaultData {
    public static final String PERMISSION_PREFIX = "RPGProfiles.Default.";
    private static final Map<String,DefaultData> map = new HashMap<>();
    public static DefaultData defaultData;
    private final Location location;
    private final double balance;
    private final @Nullable ConfigurationSection configuration;
    private final SavedClassInformation savedClassInformation;
    private final String i;
    private final String permission;

    public DefaultData(String name){
        this.i = name.toUpperCase();
        this.permission = PERMISSION_PREFIX + i.toUpperCase(Locale.ROOT);
        this.configuration = RPGProfiles.getDefaultPlayerConfig().getConfigurationSection(name);
        if (configuration == null||configuration.getKeys(false).isEmpty()){
            throw new RuntimeException("Configuration for " + i + " is missing!");
        }

        this.savedClassInformation = new SavedClassInformation(configuration);

        String worldName = configuration.getString("location.world");
        double x = configuration.getDouble("location.x");
        double y = configuration.getDouble("location.y");
        double z = configuration.getDouble("location.z");
        float yaw = (float) configuration.getDouble("location.yaw");
        float pitch = (float) configuration.getDouble("location.pitch");
        this.location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

        this.balance = configuration.getDouble("balance");
    }

    public boolean exists() {
        return configuration != null && !configuration.getKeys(false).isEmpty();
    }

    public static PlayerPermissionManager getPermissionManager(){
        return new PlayerPermissionManager();
    }
}

