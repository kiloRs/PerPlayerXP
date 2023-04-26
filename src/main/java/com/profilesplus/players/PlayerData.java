package com.profilesplus.players;

import com.profilesplus.LimboManager;
import com.profilesplus.RPGProfiles;
import com.profilesplus.events.ProfileChangeEvent;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.menu.CharSelectionMenu;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Getter
public class PlayerData {
    @Getter
    private static final NamespacedKey ACTIVE_PROFILE_KEY = new NamespacedKey(RPGProfiles.getInstance(), "activeProfile");
    @Getter
    private static final Map<UUID, PlayerData> playerDataInstances = new HashMap<>();

    private final UUID uuid;
    private final File playerFile;
    private final MMOPlayerData mythicLib;
    private FileConfiguration config;

    private final Map<Integer, Profile> profileMap = new HashMap<>();

    private final Player player;
    private Profile activeProfile;
    private DefaultData defaultData;
    private int activeSlot;
    @Getter
    private static final String PERMISSION_PREFIX = "rpgprofiles.slot.";

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

        this.mythicLib = MMOPlayerData.get(uuid);
        this.activeProfile = null;

        loadConfig();


        Set<String> set = RPGProfiles.getDefaultPlayerConfig().getKeys(false);

        boolean keyFounD = false;
        if (!set.isEmpty()) {
            for (String key : set) {
                if (key.equalsIgnoreCase("DEFAULT")){
                    keyFounD = true;
                }
                if (player.hasPermission(DefaultData.PERMISSION_PREFIX + key.toUpperCase())){
                    defaultData = new DefaultData(key.toUpperCase());
                }
            }
            if (getDefaultData() == null){
                defaultData = DefaultData.get("DEFAULT");
            }
        }

        if (hasActiveKey()){
            if (getActiveKey() > 0) {
                ConfigurationSection section = config.getConfigurationSection("profiles." + getActiveKey());
                if (section != null){
                String className = section.getString("className");
                String id = section.getString("id");
                if (config.isConfigurationSection("profiles." + getActiveKey()) && config.getKeys(false).size() > 0 && section != null && className != null && id != null){
                    Profile profile = new Profile(id,className,getActiveKey(),uuid,true);

                    profileMap.put(getActiveKey(),profile);
                    setActiveProfile(profile);
                    return;
                }
                }
                return;
            }
            player.getPersistentDataContainer().remove(ACTIVE_PROFILE_KEY);
        }

        if (activeProfile == null){
            RPGProfiles.log("No Profile for Player: " + player.getName());
        }
        activateProfile();
    }

    private void activateProfile() {
        if (activeProfile != null){
            if (activeProfile.update()) {
                player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER, getActiveProfile().getIndex());
                RPGProfiles.log("Profile Activation via Player! " + activeProfile.getIndex() + " " + activeProfile.getName() + " " + activeProfile.getClassName());
            }
            setLimbo(false);
        }
        else {
            setLimbo(true);
            new CharSelectionMenu(this).open();
        }
    }

    public static PlayerData get(UUID uuid) {
        if (!playerDataInstances.containsKey(uuid)){
            playerDataInstances.put(uuid,new PlayerData(uuid));
        }

        return playerDataInstances.get(uuid);
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
        if (!playerFile.exists() || !playerFile.isFile()) {
            if (!playerFile.getParentFile().exists()){
                playerFile.getParentFile().mkdirs();
            }
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(playerFile);
    }

    private static File getPlayerDataFolder() {
        final File dataFolder = new File("plugins/RPGProfiles/playerdata");
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    public void saveActiveProfile( boolean fromPlayer){
        activeProfile.saveToConfigurationSection(fromPlayer);
    }
    public void saveProfiles() {
        for (Profile profile : profileMap.values()) {
            if (isActive(profile.getIndex())){
                profile.saveToConfigurationSection(true);
                player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER, getActiveProfile().getIndex());
                saveConfig();
            }
        }
    }

    public void loadProfiles() {
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
        if (profilesSection != null) {
            for (String slotString : profilesSection.getKeys(false)) {
                int slot = Integer.parseInt(slotString);
                if (loadProfile(slot)) {
                    RPGProfiles.debug("Profile ! ! ! ! ! ! ! ! ! ! " + slot );
                }
            }
        }

        int activeSlot = player.getPersistentDataContainer().has(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER)?player.getPersistentDataContainer().get(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER):-1;

        if (activeSlot > 0 && profileMap.containsKey(activeSlot)) {
            setActiveProfile(profileMap.get(activeSlot));
            return;
        }
        if (activeSlot > 0){
            if (loadProfile(activeSlot)) {
                RPGProfiles.log("Profile Initiate: " + activeSlot);
            }
            if (profileMap.containsKey(activeSlot)){
                setActiveProfile(profileMap.get(activeSlot));
            }
        }
    }
    public boolean loadProfile(int x) {
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
        if (profilesSection != null) {
            String slotString = String.valueOf(x);
            if (profilesSection.contains(slotString)) {
                String displayName = profilesSection.getString(slotString + ".id");
                String className = profilesSection.getString(slotString + ".className");
                if (displayName != null && className != null && !displayName.isEmpty() && !className.isEmpty() && MMOCore.plugin.classManager.has(className)) {
                    Profile profile = new Profile(displayName, className, x, uuid, true);
                    profileMap.put(x, profile);
                    return true;
                } else if (className != null && !MMOCore.plugin.classManager.has(className.toUpperCase())) {
                    RPGProfiles.log("Error: Profile " + x + " of " + displayName + " of player: " + player.getName() + " has a broken class name!");
                } else {
                    RPGProfiles.log("Configuration Error of Profiles : " + x + " " + player.getName());
                }
            }
        }
        return false;
    }

    public void createNewProfile(String displayName, String className , boolean activate) {
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
            return;
        }

        Profile newProfile = new Profile(displayName, className, availableSlot,uuid,false);

        ProfileCreateEvent event = new ProfileCreateEvent(this, newProfile,activate, false);
        Bukkit.getPluginManager().callEvent(event);

    }
    public void createNewProfileInSlot(String displayName, String className, boolean activate, int slot, boolean over) {
        if (slot < 1 || slot > 15) {
            player.sendMessage("Invalid slot number.");
            return;
        }

        if (slot >= 5 && !player.hasPermission(PERMISSION_PREFIX + slot)) {
            player.sendMessage("You don't have permission to create a profile in this slot.");
            return;
        }

        for (Profile profile : profileMap.values()) {
            if (profile.getIndex() == slot && !over) {
                player.sendMessage("This slot is already occupied.");
                return;
            }
        }

        Profile newProfile = new Profile(displayName, className,slot,uuid,false);

        ProfileCreateEvent event = new ProfileCreateEvent(this, newProfile, activate, over);
        Bukkit.getPluginManager().callEvent(event);

    }

    public void setActiveProfile(Profile profile){
        this.setActiveProfile(profile.getIndex(),null);
    }
    public void setActiveProfile(int newSlot, String settingsName) {
        if (activeProfile != null) {
            activeProfile.saveToConfigurationSection(true);
        }

        if (profileMap.containsKey(newSlot)) {
            // Load the new profile's inventory from the database
            Profile newProfile = profileMap.get(newSlot);
            if (newProfile != null) {
                this.activeProfile = newProfile;
                this.activeSlot = newSlot;
                player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER, getActiveProfile().getIndex());
                player.saveData();




                if (activeProfile.update()){
                    player.getPersistentDataContainer().set(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER,activeSlot);

                    if (activeProfile.hasSavedInventory()) {
                        activeProfile.loadInventory();
                    }
                    else {
                        RPGProfiles.debug("No Inventory Save for " + activeProfile.getIndex());
                    }
                    RPGProfiles.debug("Active Profile Updated from setActiveProfile(" + activeProfile.getIndex() +  ")");
                }



                activeProfile.saveToConfigurationSection(true,true,RPGProfiles.isUsingEconomy());
                setLimbo(false);
                return;
            }
            else {
                RPGProfiles.log("Error: Profile is not save or register currently in " + player.getName() + " 's profile!");

                if (profileMap.isEmpty() || activeProfile == null){
                    setLimbo(activeProfile==null);
                }
                return;
            }
        }
        RPGProfiles.log("Profile in slot: " + newSlot + " not available to be activeProfile of " + player.getName());
    }

    public void setLimbo(boolean limbo) {

        LimboManager m = RPGProfiles.getLimboManager();
        if (limbo) {
            m.setWaiting(player,m.hasOriginalLocation(player)?m.getOriginalLocation(player):player.getLocation().toBlockLocation()==m.getSpectatorLocation().toBlockLocation()?null:player.getLocation().toBlockLocation());
            player.sendMessage(RPGProfiles.getMessage(player,"limbo.enter","&eYou have been placed into Limbo mode! Please select or create a profile (/profiles)"));
        }
        else {
            m.removeWaiting(player);
            player.sendMessage(RPGProfiles.getMessage(player,"limbo.exit","&eYou have been taken out of Limbo mode!"));
        }
    }


    /**
     * @return Unmodifiable Map of Profiles
     */
    public Map<Integer, Profile> getProfiles() {
        return Collections.unmodifiableMap(profileMap);
    }


    public int findFirstAvailableProfileSlot() {
        for (int i = 0; i < 15; i++) {
            if (!this.getProfileMap().containsKey(i)) {
                if (i <= 5 || this.getPlayer().hasPermission(PERMISSION_PREFIX + i + 1)) {
                    return i + 1;
                }
            }
        }
        return -1; // Return -1 if no available slot is found
    }
    public boolean changeProfile(@NotNull(value = "Empty Profile Error!") Profile newProfile, @Nullable String settings) {
        @Nullable Profile oldProfile = getActiveProfile();


        if (oldProfile!= null)
            oldProfile.saveToConfigurationSection(true);

        if (!newProfile.hasSaveInConfig()){
            if (settings == null){
                settings = DefaultData.defaultData.getI().toUpperCase();
            }
        }
        // Fire the ProfileChangeEvent
        ProfileChangeEvent profileChangeEvent = new ProfileChangeEvent(PlayerData.get(player), oldProfile, newProfile, settings);
        Bukkit.getServer().getPluginManager().callEvent(profileChangeEvent);

        // Check if the event was cancelled or a new profile was set
        if (!profileChangeEvent.isCancelled()) {
            setActiveProfile(profileChangeEvent.getNewProfile().getIndex(),profileChangeEvent.getSettings());
            return true;
       }
        else {
            RPGProfiles.debug("The profile change event was stopped! Please check into this!");
        }
        return false;
    }

    public boolean removeProfile(Profile profile) {
        if (!profileMap.containsValue(profile)){
            return false;
        }
        if (activeSlot == profile.getIndex()){
            return false;
        }
        int number = profile.getIndex();
        getProfileMap().remove(number);
        config.set("profiles." + number,null);
        profile = null;
        return true;
    }

    public boolean isActive(int index) {
        return activeSlot==index&& activeProfile!=null && profileMap.containsKey(index) && activeProfile == profileMap.get(index);
    }

    public int getActiveKey(){

        return player.getPersistentDataContainer().getOrDefault(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER, 0);
    }
    public boolean hasActiveKey(){
        Integer integer = player.getPersistentDataContainer().getOrDefault(ACTIVE_PROFILE_KEY, PersistentDataType.INTEGER, 0);
        return player.getPersistentDataContainer().has(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER) && integer.intValue()>0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PlayerData that)) return false;

        return new EqualsBuilder().append(getActiveSlot(), that.getActiveSlot()).append(getUuid(), that.getUuid()).append(getPlayerFile(), that.getPlayerFile()).append(getProfileMap(), that.getProfileMap()).append(getActiveProfile(), that.getActiveProfile()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getUuid()).append(getPlayerFile()).append(getProfileMap()).append(getActiveProfile()).append(getActiveSlot()).toHashCode();
    }


}
