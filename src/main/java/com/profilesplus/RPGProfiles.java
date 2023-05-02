package com.profilesplus;

import com.profilesplus.commands.*;
import com.profilesplus.configs.ProfileConfigManager;
import com.profilesplus.configs.StatHandler;
import com.profilesplus.listeners.*;
import com.profilesplus.players.PlayerData;
import com.profilesplus.saving.InventoryManager;
import com.profilesplus.saving.ProfileSavingManager;
import io.lumine.mythic.lib.MythicLib;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Getter
public final class RPGProfiles extends JavaPlugin {
    @Getter
    private static Plugin instance;
    @Getter
    private static ProfileConfigManager profileConfigManager;

    @Getter
    private static LimboManager limboManager;
    @Getter
    private static Economy economy;
    @Getter
    private static boolean usingEconomy = false;
    @Getter
    private static FileConfiguration messagesConfig;
    @Getter
    private static YamlConfiguration defaultPlayerConfig;
    @Getter
    private static StatHandler statHandler;
    @Getter
    private static ProfileSavingManager savingManager;
    private static File defaultPlayerFile;


    public void createDefaultPlayerConfig() {
        defaultPlayerFile = new File(getDataFolder(), "defaultClassValues.yml");
        if (!defaultPlayerFile.exists()) {
            saveResource("defaultClassValues.yml", false);
        }
        defaultPlayerConfig = YamlConfiguration.loadConfiguration(defaultPlayerFile);
        if (defaultPlayerConfig.getKeys(false).isEmpty()){
            saveResource("defaultClassValues.yml",true);
        }
        defaultPlayerConfig = YamlConfiguration.loadConfiguration(defaultPlayerFile);


    }

    public static void log(String s) {
        Logger.getLogger("Minecraft").info("[RPGProfiles] " + s);
    }

    public static IconsManager getIcons(Player player) {
        return new IconsManager(player);
    }

    public static boolean isLogging() {
        return instance.getConfig().contains("debug") && instance.getConfig().getBoolean("debug",false);

    }

    private static String parsePlaceholder(Player player,String message) {
        String replace = message.replace("%naming_max_length%", String.valueOf(profileConfigManager.getMaxNameLength()))
                .replace("%naming_min_length%", String.valueOf(profileConfigManager.getMinNameLength()));
        return PlaceholderAPI.setPlaceholders(player,replace);
    }
    public static String getMessage(Player p,String message, String efault) {
        String mess = getMessage(message,efault);
        return MythicLib.inst().parseColors(parsePlaceholder(p,mess));

    }

    public static void debug(String message) {
        Logger.getLogger("Minecraft").severe("[RPGProfiles debug system]: " + message);
    }

    @Override
    public void onEnable() {
        instance = this;

        loadMessagesConfig();

        createDefaultPlayerConfig();

        saveDefaultConfig();

        savingManager = new ProfileSavingManager();
        statHandler = new StatHandler(this);
        limboManager = new LimboManager(this);


        profileConfigManager = new ProfileConfigManager(this);

        registerListeners();
        register();


        if (!setupEconomy()){
            log("Requires Vault Plugin!");
        }
        else {
            usingEconomy = true;
            log("Vault plugin hook successful!");
        }
        Logger.getLogger("Minecraft").severe("Successfully Loaded RPGProfiles Plugin!");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            new ProfilePAPI(this).register();
            log("Registering PlaceholderAPI Expansion from ProfilesPlus");
        }
    }

    private void register(){
        PluginCommand profiles = Bukkit.getPluginCommand("profiles");
        PluginCommand create = Bukkit.getPluginCommand("createProfile");
        PluginCommand remove = Bukkit.getPluginCommand("removeProfile");
        PluginCommand save = Bukkit.getPluginCommand("saveProfiles");
        PluginCommand reload = Bukkit.getPluginCommand("rpgprofiles");
        if (profiles != null){
            profiles.setExecutor(new ProfilesCommand(this));
        }
        if (create != null){
            create.setExecutor(new ProfileCreateCommand(this));
        }
        if (remove != null){
            remove.setExecutor(new DeleteProfilesCommand(this));
        }
        if (save != null){
            save.setExecutor(new SaveCommand());
        }
        if (reload != null){
            reload.setExecutor(new ReloadCommand());
        }
    }
    public static InventoryManager getInventoryManager(){
        return new InventoryManager("inventories.db");
    }
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerMoveRestrict(limboManager),this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(),this);
        Bukkit.getPluginManager().registerEvents(new ProfileListener(),this);

        if (getConfig().isBoolean("item-ownership.enable") && getConfig().getBoolean("item-ownership.enable")){
            if (!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")){
                log("Mythic Mobs must be existing for item-ownership from config to work!");
            }
            else {
                Bukkit.getPluginManager().registerEvents(new PlayerItemListener(this),this);
                log("Enabled the Player Item controller (to protect drops/ownership).");
            }
        }

        if (!getConfig().isInt("minsToSave")){
            getConfig().set("minsToSave",10);
            saveConfig();
        }
        int saveInterval = getConfig().getInt("minsToSave", 10);
        PlayerDataListener playerDataListener = new PlayerDataListener(this, saveInterval);
        getServer().getPluginManager().registerEvents(playerDataListener, this);
        playerDataListener.startAutoSave();
    }

    @Override
    public void onDisable() {
        Logger.getLogger("Minecraft").severe("Saving all Player files for RPGProfiles!");

        PlayerData.getPlayerDataInstances().forEach((uuid, playerData) -> {
            playerData.close();
        });

        getInventoryManager().close();
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy!=null;
    }

    private void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String path, String p) {
        File file = new File(instance.getDataFolder(), "messages.yml");

        if (!file.isFile() || !file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);

        if (!messagesConfig.isString(path)){
            messagesConfig.set(path,p);
            try {
                messagesConfig.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(path));
    }

}
