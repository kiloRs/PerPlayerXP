package com.profilesplus;

import com.profilesplus.commands.DeleteProfilesCommand;
import com.profilesplus.commands.ProfileCreateCommand;
import com.profilesplus.commands.ProfilesCommand;
import com.profilesplus.listeners.*;
import com.profilesplus.players.PlayerData;
import com.profilesplus.saving.InventoryDatabase;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Getter
public final class RPGProfiles extends JavaPlugin {
    @Getter
    private static Plugin instance;
    private SpectatorManager spectatorManager;
    private Economy economy;
    @Getter
    private static boolean usingEconomy = false;
    @Getter
    private static InventoryDatabase inventoryDatabase;
    public static void log(String s) {
        Logger.getLogger("Minecraft").info("[RPGProfiles] " + s);
    }

    public static IconsManager getIcons(Player player) {
        return new IconsManager(player);
    }

    public static boolean isLogging() {
        return instance.getConfig().contains("debug") && instance.getConfig().getBoolean("debug",false);

    }

    @Override
    public void onEnable() {
        instance = this;
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
        return getConfig().getStringList("forbiddenNames");
    }

    public void addForbidden(List<String> k){
        for (String s : k) {
            getConfig().getStringList("forbiddenNames").add(s);
        }
        try {
            getConfig().save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this),this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveRestrict(spectatorManager),this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(),this);

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
