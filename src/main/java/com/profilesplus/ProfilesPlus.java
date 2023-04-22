package com.profilesplus;

import com.profilesplus.commands.DeleteProfilesCommand;
import com.profilesplus.commands.ProfileCreateCommand;
import com.profilesplus.commands.ProfilesCommand;
import com.profilesplus.listeners.InventoryListener;
import com.profilesplus.listeners.PlayerDataListener;
import com.profilesplus.listeners.PlayerJoinListener;
import com.profilesplus.listeners.PlayerMoveRestrict;
import com.profilesplus.menu.text.ChatEventListener;
import com.profilesplus.menu.text.InputTextManager;
import com.profilesplus.players.PlayerData;
import com.profilesplus.saving.InventoryDatabase;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Getter
public final class ProfilesPlus extends JavaPlugin {
    @Getter
    private static Plugin instance;
    private InputTextManager textManager;
    private SpectatorManager spectatorManager;
    private Economy economy;
    private Chat chat;
    private Permission perms;
    private YamlConfiguration forbiddenNames = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
    @Getter
    private static boolean usingEconomy = false;
    @Getter
    private static InventoryDatabase inventoryDatabase;
    public static void log(String s) {
        Logger.getLogger("Minecraft").info("[RPGProfiles] " + s);
    }

    public static IconsManager getIcons() {
        return new IconsManager();
    }

    public static boolean isLogging() {
        return instance.getConfig().contains("debug") && instance.getConfig().getBoolean("debug",false);

    }

    @Override
    public void onEnable() {
        instance = this;
        textManager = new InputTextManager();
        spectatorManager = new SpectatorManager(this);
        inventoryDatabase = new InventoryDatabase("inventories.db");
        saveDefaultConfig();

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
        if (profiles != null){
            profiles.setExecutor(new ProfilesCommand(this));
        }
        if (create != null){
            create.setExecutor(new ProfileCreateCommand(this));
        }
        if (remove != null){
            remove.setExecutor(new DeleteProfilesCommand(this));
        }
    }
    public List<String> getForbiddenNames() {
        return forbiddenNames.getStringList("forbiddenNames");
    }

    public void addForbidden(List<String> k){
        for (String s : k) {
            forbiddenNames.getStringList("forbiddenNames").add(s);
        }
        try {
            forbiddenNames.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ChatEventListener(textManager),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this),this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveRestrict(),this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(),this);


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
        for (PlayerData allInstance : PlayerData.getAllInstances()) {
            allInstance.saveProfiles();
        }
        Logger.getLogger("Minecraft").severe("Complete Saving all Player files for RPGProfiles!");

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

}
