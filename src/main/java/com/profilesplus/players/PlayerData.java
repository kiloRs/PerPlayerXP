package com.profilesplus.players;

import com.profilesplus.RPGProfiles;
import com.profilesplus.events.ProfileChangeEvent;
import com.profilesplus.events.ProfileCreateEvent;
import com.profilesplus.events.ProfileRemoveEvent;
import com.profilesplus.menu.CharSelectionMenu;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Getter
public class PlayerData {
    @Getter
    private static final Map<UUID, PlayerData> playerDataInstances = new HashMap<>();

    private final UUID uuid;
    private final File playerFile;
    private final MMOPlayerData mythicLib;
    private FileConfiguration config;

    private final Player player;
    private final ProfileStorage profileStorage;
    private final ActiveKeyHolder keyHolder;

    private final net.Indyuce.mmocore.api.player.PlayerData mmoCore;

    public PlayerData(Player player) {
        this(player.getUniqueId());
    }

    private PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getOfflinePlayer(uuid).getPlayer();
        if (player == null) {
            throw new RuntimeException("Player error!");
        }
        this.playerFile = new File(getPlayerDataFolder(), uuid + ".yml");

        this.mythicLib = MMOPlayerData.get(uuid);
        this.profileStorage = new ProfileStorage(this);
        loadConfig();

        this.mmoCore = net.Indyuce.mmocore.api.player.PlayerData.get(uuid);
        this.keyHolder = new ActiveKeyHolder(this.player);
        if (mmoCore == null) {
            throw new RuntimeException("MMOCore failure!");
        }

    }

    public boolean loadActive() {
        if (keyHolder.hasActive()) {
            ConfigurationSection section = config.getConfigurationSection("profiles." + keyHolder.getActive());
            if (section != null) {
                String className = section.getString("className");
                String id = section.getString("id");
                if (config.isConfigurationSection("profiles." + getKeyHolder().getActive()) && config.getKeys(false).size() > 0 && className != null && id != null) {
                    Profile profile = new Profile(id, className, getKeyHolder().getActive(), uuid, true);

                    profileStorage.putProfile(profile);
                    setActiveProfile(profile);
                    return true;
                }
            }
            initiateProfile();
            return true;
        }
        keyHolder.resetActive();
        return false;
    }

    private void initiateProfile() {
        if (!profileStorage.hasActiveProfile()) {
            new CharSelectionMenu(this).open();
        } else {
            if (!profileStorage.getActiveProfile().isCreated()) {
                RPGProfiles.getSavingManager().saveData(profileStorage.getActiveProfile(), false);
                RPGProfiles.getSavingManager().saveInventory(profileStorage.getActiveProfile());
            }
            if (profileStorage.getActiveProfile().update(true)) {
                RPGProfiles.log("Profile Activation Complete!");
            }
        }
    }


    public static PlayerData get(UUID uuid) {
        if (!playerDataInstances.containsKey(uuid)) {
            if (!net.Indyuce.mmocore.api.player.PlayerData.has(uuid)) {
                RPGProfiles.debug("MMOCore Player has not loaded!");
            }
            playerDataInstances.put(uuid, new PlayerData(uuid));
        }

        return playerDataInstances.get(uuid);
    }

    public static PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public static PlayerData[] getAllInstances() {
        return playerDataInstances.values().toArray(new PlayerData[0]);
    }

    public static boolean exists(Player p) {
        return PlayerData.getPlayerDataInstances().containsKey(p.getUniqueId());
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
            if (!playerFile.getParentFile().exists()) {
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

    public void saveActiveProfile(boolean fromPlayer) {
        if (profileStorage.hasActiveProfile())
            RPGProfiles.getSavingManager().saveData(profileStorage.getActiveProfile(), fromPlayer);
//        activeProfile.saveToConfigurationSection(fromPlayer);
    }

    public static Profile loadActive(Player player, int index) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.hasPermissionFor(index)) {
            if (playerData.loadProfile(index)) {
                RPGProfiles.log("Profile Reloaded: " + index);
                playerData.setActiveProfile(index, !playerData.profileStorage.get(index).isCreated());
                return playerData.profileStorage.getActiveProfile();
            } else {
                return null;
            }
        }
        return null;
    }

    public void reload() {
        loadConfig();
        loadProfiles();


        if (!keyHolder.hasActive()) {
            RPGProfiles.debug("NO ACTIVE KEY FOR PLAYER: " + player.getName().toUpperCase());
            return;
        }
        if (profileStorage.isEmpty()){
            RPGProfiles.debug("NO PROFILES FOR PLAYER: " + player.getName().toUpperCase());
        }
        Profile profile = loadActive(player, getKeyHolder().getActive());

        if (profile != null) {
            profile.reload();
            RPGProfiles.log("Successfully Reloaded Profile: " + profile.getId() + " [" + profile.getIndex() + "]");
        } else {
            RPGProfiles.log("No Active Profile to Reload for: " + player.getName().toUpperCase());
        }
    }

    public void close() {
        if (profileStorage.hasActiveProfile()) {
            this.saveActiveProfile(profileStorage.getActiveProfile().isCreated());

            if (keyHolder.hasActive()){
                if (keyHolder.getActive() != profileStorage.getActiveProfile().getIndex()){
                    keyHolder.setActive(profileStorage.getActiveProfile());
                }
            }
            else {
                keyHolder.setActive(profileStorage.getActiveProfile());
            }
            return;
        }
        RPGProfiles.log("No Playerdata Close for " + player.getName() + " because no active profile was found!");
    }

    public void loadProfiles() {
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
        if (profilesSection != null) {
            for (String slotString : profilesSection.getKeys(false)) {
                int slot = Integer.parseInt(slotString);
                if (loadProfile(slot)) {
                    RPGProfiles.debug("Profile ! ! ! ! ! ! ! ! ! ! " + slot);
                }
            }
        }

//        int activeSlot = player.getPersistentDataContainer().has(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER)?player.getPersistentDataContainer().get(ACTIVE_PROFILE_KEY,PersistentDataType.INTEGER):-1;
//
//        if (activeSlot > 0 && profileMap.containsKey(activeSlot)) {
//            setActiveProfile(profileMap.get(activeSlot));
//            return;
//        }
//        if (activeSlot > 0){
//            if (loadProfile(activeSlot)) {
//                RPGProfiles.log("Profile Initiate: " + activeSlot);
//            }
//            if (profileMap.containsKey(activeSlot)){
//                setActiveProfile(profileMap.get(activeSlot));
//            }
//        }

        if (loadActive()) {
            RPGProfiles.debug("Active Load");
            return;
        }
        RPGProfiles.debug("Active Not Loaded!");

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
                    profileStorage.putProfile(profile);
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

    public void createNewProfile(String displayName, String className, boolean activate) {
        int maxSlots = 15;
        int availableSlot = -1;

        // Find the first available slot
        for (int i = 0; i < maxSlots; i++) {
            boolean slotOccupied = false;

            for (Profile profile : profileStorage.getAll()) {
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

        Profile newProfile = new Profile(displayName, className, availableSlot, uuid, false);

        ProfileCreateEvent event = new ProfileCreateEvent(this, newProfile, activate, false);
        Bukkit.getPluginManager().callEvent(event);

    }

    public boolean hasPermissionFor(int slotNumber) {
        return player.hasPermission("rpgprofiles.slots." + slotNumber) || slotNumber > 0 && slotNumber < 6;
    }

    public void createNewProfileInSlot(String displayName, String className, boolean activate, int slot, boolean over) {
        if (slot < 1 || slot > 15) {
            throw new RuntimeException("Invalid Slot Use Number from Creating Profile Menu");
        }

        if (hasPermissionFor(slot)) {
            player.sendMessage("You don't have permission to create a profile in this slot.");
            return;
        }

        for (Profile profile : profileStorage.getAll()) {
            if (profile.getIndex() == slot && !over) {
                player.sendMessage("This slot is already occupied.");
                return;
            }
        }

        Profile newProfile = new Profile(displayName, className, slot, uuid, false);

        ProfileCreateEvent event = new ProfileCreateEvent(this, newProfile, activate, over);
        Bukkit.getPluginManager().callEvent(event);

    }

    public void setActiveProfile(Profile profile) {
        this.setActiveProfile(profile.getIndex(), !profile.isFromFile());
    }

    public void setActiveProfile(int newSlot, boolean fresh) {
        if (profileStorage.hasActiveProfile() && profileStorage.getActiveProfile().getIndex() != newSlot) {
            RPGProfiles.getSavingManager().saveInventory(profileStorage.getActiveProfile());
            RPGProfiles.getSavingManager().saveData(profileStorage.getActiveProfile(), !fresh);
            profileStorage.getActiveProfile().getInternalPlayer().getPlayer().getInventory().clear();
            profileStorage.getActiveProfile().setCreated(true);
        } else if (profileStorage.getActiveProfile() != null) {
            RPGProfiles.getSavingManager().saveData(profileStorage.getActiveProfile(), !fresh);
            RPGProfiles.getSavingManager().saveInventory(profileStorage.getActiveProfile());
            return;
        }

        if (profileStorage.hasProfile(newSlot)) {
            // Load the new profile's inventory from the database
            Profile newProfile = profileStorage.get(newSlot);
            if (newProfile != null) {
                this.profileStorage.setActiveProfile(newProfile);
                if (profileStorage.getActiveProfile().update(true)) {
                    RPGProfiles.debug("Loaded Profile: " + profileStorage.getActiveProfile().getId());
                } else {
                    throw new RuntimeException("Save Load Order Error in Player");
                }
            } else {
                throw new RuntimeException("Unregistered Profile Error");
            }
        }
    }


    public int findFirstAvailableProfileSlot() {
        for (int i = 0; i < 15; i++) {
            int number = i + 1;

            if (!this.profileStorage.hasProfile(i + 1)) {
                if (hasPermissionFor(number)) {
                    return number;
                }
            }
        }
        return -1; // Return -1 if no available slot is found
    }

    public boolean changeProfile(@NotNull(value = "Empty Profile Error!") Profile newProfile, boolean fresh) {
        @Nullable Profile oldProfile = getProfileStorage().getActiveProfile();


        if (oldProfile != null)
            RPGProfiles.getSavingManager().saveData(oldProfile, true);

        // Fire the ProfileChangeEvent
        ProfileChangeEvent profileChangeEvent = new ProfileChangeEvent(PlayerData.get(player), oldProfile, newProfile, fresh);
        Bukkit.getServer().getPluginManager().callEvent(profileChangeEvent);

        // Check if the event was cancelled or a new profile was set
        if (!profileChangeEvent.isCancelled()) {
            setActiveProfile(profileChangeEvent.getNewProfile().getIndex(), fresh);
            return true;
        } else {
            RPGProfiles.debug("The profile change event was stopped! Please check into this!");
        }
        return false;
    }

    public boolean removeProfile(Profile profile) {
        if (!getProfileStorage().hasProfile((profile))) {
            return false;
        }
        ProfileRemoveEvent event = new ProfileRemoveEvent(profile);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }
        int number = event.getProfile().getIndex();
        profileStorage.remove(number);
        PlayerData.get(event.getPlayer()).config.set("profiles." + number, null);
        profile = null;
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PlayerData that)) return false;

        return new EqualsBuilder().append(getUuid(), that.getUuid()).append(getPlayer(), that.getPlayer()).append(profileStorage, that.profileStorage).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getUuid()).append(profileStorage).toHashCode();
    }

    public boolean canChangeProfiles() {
        if (RPGProfiles.getLimboManager().isWaiting(player)) {
            return true;
        }
        return mmoCore.isInCombat() || mmoCore.isCasting() || player.isFlying() || player.isSleeping() || player.isDead();
    }
}
