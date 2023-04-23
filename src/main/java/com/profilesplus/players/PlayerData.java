package com.profilesplus.players;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.saving.BukkitSerialization;
import com.profilesplus.saving.InventoryDatabase;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Getter
public class PlayerData {
    @Getter
    private static final NamespacedKey ACTIVE_PROFILE_KEY = new NamespacedKey(ProfilesPlus.getInstance(), "activeProfile");
    @Getter
    private static final Map<UUID, PlayerData> playerDataInstances = new HashMap<>();

    private final UUID uuid;
    private final File playerFile;
    private final net.Indyuce.mmoitems.api.player.PlayerData mmoItems;
    private final MMOPlayerData mythicLib;
    private YamlConfiguration config;
    //The storage of the profiles
    private final Map<Integer, Profile> profileMap = new HashMap<>();

    private final Player player;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCore;
    private Profile activeProfile;
    private int activeSlot;
    @Getter
    private static final String PERMISSION_PREFIX = "rpgprofiles.slot.";
    private static final InventoryDatabase inventoryDatabase = ProfilesPlus.getInventoryDatabase();

    public PlayerData(Player player){
        this(player.getUniqueId());
    }
    private PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getOfflinePlayer(uuid).getPlayer();
        if (player == null){
            throw new RuntimeException("Player error!");
        }
        this.playerFile = new File(getPlayerDataFolder(), uuid + ".yml");
        this.mmoCore = net.Indyuce.mmocore.api.player.PlayerData.get(uuid);
        this.mmoItems = net.Indyuce.mmoitems.api.player.PlayerData.get(uuid);
        this.mythicLib = MMOPlayerData.get(uuid);
        this.activeProfile = null;

        loadConfig();

        // Check if the profiles section contains the active profile
        int act = player.getPersistentDataContainer().getOrDefault(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER,0);
        if (act > 0) {
            if (config.isConfigurationSection("profiles." + act) && !config.getConfigurationSection("profiles." + act).getKeys(false).isEmpty()) {
                String id = config.getString("profiles." + act + ".id", null);
                this.activeProfile = new Profile(id, config.getString("profiles." + act + ".className"), act, uuid);
            }

            activateProfile();
        }
        if (activeProfile == null){
            ProfilesPlus.log("No Active Profile - Opening Profiles Menu!");
            new ProfilesMenu(ProfilesPlus.getInstance(),this).open();
        }
        else {
            this.activeSlot = activeProfile.getIndex();
        }
    }

    private void activateProfile() {
        if (activeProfile != null){
            activeProfile.update();
            ProfilesPlus.log("Profile Activation via Player! " + activeProfile.getIndex() + " " + activeProfile.getName() + " " + activeProfile.getClassName());
        }
    }

    public static PlayerData get(UUID uuid) {
        return playerDataInstances.computeIfAbsent(uuid, k -> new PlayerData(uuid));
    }

    public static PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public static PlayerData[] getAllInstances() {
        return playerDataInstances.values().toArray(new PlayerData[0]);
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

        loadProfiles();
    }

    private static File getPlayerDataFolder() {
        final File dataFolder = new File("plugins/ProfilesPlus/playerdata");
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    public void saveProfiles() {
        for (Profile profile : profileMap.values()) {
            profile.saveToConfigurationSection();
        }

        if (getActiveProfile() != null) {
            player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER, getActiveProfile().getIndex());
            player.saveData();
        }
        saveConfig();
    }

    public void loadProfiles() {
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
        if (profilesSection != null) {
            for (String slotString : profilesSection.getKeys(false)) {
                int slot = Integer.parseInt(slotString);
                String displayName = profilesSection.getString(slotString + ".id");
                String className = profilesSection.getString(slotString + ".className");
                if (displayName != null && className != null) {
                    Profile profile = new Profile(displayName, className, slot, uuid);
                    profileMap.put(slot, profile);
                }
            }
        }

        int activeSlot = player.getPersistentDataContainer().has(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER)?player.getPersistentDataContainer().get(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER):-1;

        if (activeSlot > 0 && profileMap.containsKey(activeSlot) && profileMap.get(activeSlot).isCreated()) {
            activeProfile =  profileMap.get(activeSlot);
        }
    }

    public Profile createNewProfile(String displayName, String className , boolean activate) {
        int maxSlots = 15;
        int availableSlot = -1;

        // Find the first available slot
        for (int i = 0; i < maxSlots; i++) {
            boolean slotOccupied = false;

            for (Profile profile : profileMap.values()) {
                if (profile.getIndex() == i) {
                    slotOccupied = true;
                    break;
                }
            }

            if (!slotOccupied) {
                if (i < 5 || player.hasPermission("profilesplus.slot." + (i + 1))) {
                    availableSlot = i;
                    break;
                }
            }
        }

        if (availableSlot == -1) {
            player.sendMessage("You have reached the maximum number of profiles.");
            return null;
        }

        Profile newProfile = new Profile(displayName, className, availableSlot,uuid);

        ProfileCreateEvent event = new ProfileCreateEvent(this, newProfile);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        profileMap.put(availableSlot, event.getProfile()); // Add the new profile to the map

        profileMap.get(availableSlot).saveToConfigurationSection();

        if (activate){
            setActiveProfile(profileMap.get(availableSlot));
        }


        return event.getProfile();
    }
    public Profile createNewProfileInSlot(String displayName, String className, boolean activate, int slot) {
        if (slot < 1 || slot > 15) {
            player.sendMessage("Invalid slot number.");
            return null;
        }

        if (slot >= 5 && !player.hasPermission("profilesplus.slot." + (slot + 1))) {
            player.sendMessage("You don't have permission to create a profile in this slot.");
            return null;
        }

        for (Profile profile : profileMap.values()) {
            if (profile.getIndex() == slot) {
                player.sendMessage("This slot is already occupied.");
                return null;
            }
        }

        Profile newProfile = new Profile(displayName, className,slot,uuid);

        ProfileCreateEvent event = new ProfileCreateEvent(this, newProfile);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        profileMap.put(slot, event.getProfile()); // Add the new profile to the map

        newProfile.saveToConfigurationSection();

        if (activate){
            setActiveProfile(profileMap.get(slot));
        }

        return event.getProfile();
    }

    public void setActiveProfile(Profile profile){
        this.setActiveProfile(profile.getIndex());
    }
    public void setActiveProfile(int newSlot) {
        if (activeProfile != null) {
            if (activeProfile.getIndex() == newSlot) {
                return;
            }
            if (activeProfile.getId().equalsIgnoreCase(getProfiles().get(newSlot).getId()) && getProfiles().containsKey(newSlot)) {
                return;
            }

            activeProfile.saveToConfigurationSection();
        }

        if (profileMap.containsKey(newSlot)) {
            // Save the current active profile's inventory to the database
            Profile currentProfile = getActiveProfile();
            if (currentProfile != null) {
                Inventory inventory = player.getInventory();
                byte[] inventoryData = BukkitSerialization.serializeInventory(inventory);
                inventoryDatabase.saveInventory(uuid.toString(), currentProfile.getIndex(), inventoryData);
            }

            // Load the new profile's inventory from the database
            Profile newProfile = profileMap.get(newSlot);
            if (newProfile != null) {
                this.activeProfile = newProfile;
                byte[] inventoryData = inventoryDatabase.loadInventory(uuid.toString(), newProfile.getIndex());
                if (inventoryData != null) {
                    Inventory deserializedInventory = BukkitSerialization.deserializeInventory(inventoryData);
                    updatePlayerInventory(player, deserializedInventory);
                }
                activeProfile.update();

                if (ProfilesPlus.isUsingEconomy()) {
                    Economy economy = ((ProfilesPlus) ProfilesPlus.getInstance()).getEconomy();
                    double currentBalance = economy.getBalance(player);
                    economy.withdrawPlayer(player, currentBalance);
                    economy.depositPlayer(player, newProfile.getBalance());
                }


                player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER,newSlot);
                saveConfig();
                return;
            }
            else {
                ProfilesPlus.log("Error: Profile is not save or register currently in " + player.getName() + " 's profile!");

                if (profileMap.isEmpty()){
                    setLimbo(activeProfile==null);
                }
                return;
            }
        }
        ProfilesPlus.log("Profile in slot: " + newSlot + " not available to be activeProfile of " + player.getName());
    }

    private void setLimbo(boolean limbo) {

        if (limbo) {
            ((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager().setWaiting(player);
        }
        else {
            ((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager().removeWaiting(player);
        }
    }


    /**
     * @return Unmodifiable Map of Profiles
     */
    public Map<Integer, Profile> getProfiles() {
        return Collections.unmodifiableMap(profileMap);
    }


    public int findFirstAvailableProfileSlot() {
        for (int i = 1; i <= 15; i++) {
            if (!this.getProfileMap().containsKey(i)) {
                if (i <= 5 || this.getPlayer().hasPermission(PERMISSION_PREFIX + i)) {
                    return i;
                }
            }
        }
        return -1; // Return -1 if no available slot is found
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PlayerData that)) return false;


        return new EqualsBuilder().append(uuid, that.uuid).append(playerFile, that.playerFile).append(mmoItems, that.mmoItems).append(mythicLib, that.mythicLib).append(config, that.config).append(profileMap, that.profileMap).append(player, that.player).append(mmoCore, that.mmoCore).append(activeProfile, that.activeProfile).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).append(playerFile).append(mmoItems).append(mythicLib).append(config).append(profileMap).append(player).append(mmoCore).append(activeProfile).toHashCode();
    }
    private void updatePlayerInventory(Player player, Inventory inventory) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            playerInventory.setItem(i, item);
        }
    }
    public void removeProfile(Profile profile) {
        int number = profile.getIndex();
        getProfileMap().remove(number);
        config.set("profiles." + number,null);
        profile = null;
    }

}
