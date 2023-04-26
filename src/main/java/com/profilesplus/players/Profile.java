package com.profilesplus.players;

import com.profilesplus.LimboManager;
import com.profilesplus.RPGProfiles;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.explorer.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

import static com.profilesplus.RPGProfiles.economy;


@Getter
public class Profile {
    private final String id;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCorePlayer;
    private final PlayerData internalPlayer;
    private final boolean fromFile;
    private SavedClassInformation classInformation;
    private final String className;
    private ConfigurationSection section;
    private final long creationTime;
    @Setter
    private boolean created = false;
    @Setter
    private Location lastKnownLocation;
    private final int index;
    private final ProfileBalance profileBalance;
    private boolean containsValues=false;

    public Profile(String id, String className, int slot, UUID num){
        this(id,className,slot,num,false);
    }
    public Profile(String id, String className,int slotNumber, UUID player, boolean fromFile){
        this.id = id;
        this.index = slotNumber;
        this.className = className;
        if (!MMOCore.plugin.classManager.has(className.toUpperCase())){
            throw new RuntimeException("Profile Class Not In MMOCORE: " + className);
        }
        this.mmoCorePlayer = MMOCore.plugin.dataProvider.getDataManager().get(player);
        this.internalPlayer = PlayerData.get(player);
        this.fromFile = fromFile;
        this.section = internalPlayer.getConfig().getConfigurationSection("profiles." + this.index);
        this.profileBalance = new ProfileBalance(this);

        if (this.section == null || !fromFile){
            this.section = internalPlayer.getConfig().createSection("profiles." + index);

            if (internalPlayer.getDefaultData() != null){
                this.classInformation = internalPlayer.getActiveProfile().getClassInformation();
                this.lastKnownLocation = internalPlayer.getActiveProfile().getLastKnownLocation();
                this.containsValues = true;
            }
        }
        else {
            this.classInformation = new SavedClassInformation(section.isConfigurationSection(className) ? section.getConfigurationSection(className) : section.createSection(className));
        }
        this.creationTime = section.isLong("creation-time")?section.getLong("creation-time",System.currentTimeMillis()):System.currentTimeMillis();

        if (containsValues){
            return;
        }
        this.lastKnownLocation = loadLocation();

    }
    public Profile(String id, String className, int slot, Player player) {
        this(id,className,slot,player.getUniqueId());
    }
    private void assignName() {
        String name = getName();
        internalPlayer.getPlayer().displayName(Component.text(name));
        internalPlayer.getPlayer().playerListName(Component.text(name));
        internalPlayer.getPlayer().customName(Component.text(MythicLib.plugin.parseColors(name + "&e[" + internalPlayer.getPlayer().getName() + "]")));

    }

    public boolean update() {
        if (!(internalPlayer.isActive(index))){
            RPGProfiles.debug("Active not found for " + index + " of " + internalPlayer.getPlayer().getName());
            return false;
        }
        // Save the current active profile if it exists
        new BukkitRunnable(){
            @Override
            public void run() {
                updateMMOCore();
                assignName();
                if (lastKnownLocation != null) {
                    LimboManager manager = RPGProfiles.getLimboManager();
                    if (lastKnownLocation.toBlockLocation().equals(manager.getSpectatorLocation().toBlockLocation())){
                        if (manager.hasOriginalLocation(getInternalPlayer().getPlayer())){
                            lastKnownLocation = manager.getOriginalLocation(internalPlayer.getPlayer());
                        }
                        else {
                            lastKnownLocation = manager.getOriginalLocation(internalPlayer.getPlayer());
                        }
                    }
                    internalPlayer.getPlayer().teleport(lastKnownLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
                // Set the player's balance to the last saved balance of the active profile


                profileBalance.loadBalance();
                loadInventory();

            }
        }.runTask(RPGProfiles.getInstance());
        return true;
    }
    private void updateMMOCore(){

        classInformation.load(MMOCore.plugin.classManager.getOrThrow(className), net.Indyuce.mmocore.api.player.PlayerData.get(internalPlayer.getUuid()));
    }

    public void saveToConfigurationSection(boolean fromPlayer){
        if (fromPlayer){
            classInformation = new SavedClassInformation(mmoCorePlayer);
            lastKnownLocation = internalPlayer.getPlayer().getLocation().toBlockLocation();
            profileBalance.saveBalance(fromPlayer);
        }
        else {
            if (containsValues){
                saveToConfigurationSection(true,true,RPGProfiles.isUsingEconomy());
            }
            else{
                throw new RuntimeException("No Values for " + this.index + " (" + this.getName() + ")");
            }
        }
        saveToConfigurationSection(true,true,RPGProfiles.isUsingEconomy() && economy != null );
    }
    public void saveToConfigurationSection(boolean location, boolean inventory, boolean balance) {

        section.set("id", id);
        section.set("classType", className.toUpperCase());
        if (!section.contains("creation-time")) {
            section.set("creation-time", creationTime);
        }

        ConfigurationSection classInformationSection = section.isConfigurationSection(className) ?
                section.getConfigurationSection(className) : section.createSection(className);


        if (classInformationSection == null){
            throw new RuntimeException("No Class Information in " + id);
        }
        // Save all properties of SavedClassInformation
        classInformationSection.set("level", classInformation.getLevel());
        classInformationSection.set("experience", classInformation.getExperience());
        classInformationSection.set("skill-points", classInformation.getSkillPoints());
        classInformationSection.set("attribute-points", classInformation.getAttributePoints());
        classInformationSection.set("attribute-realloc-points", classInformation.getAttributeReallocationPoints());
        classInformationSection.set("skill-reallocation-points", classInformation.getSkillReallocationPoints());
        classInformationSection.set("skill-tree-reallocation-points", classInformation.getSkillTreeReallocationPoints());
        classInformationSection.set("health", classInformation.getHealth());
        classInformationSection.set("mana", classInformation.getMana());
        classInformationSection.set("stamina", classInformation.getStamina());
        classInformationSection.set("stellium", classInformation.getStellium());

        ConfigurationSection attributeSection = classInformationSection.createSection("attribute");
        classInformation.mapAttributeLevels().forEach(attributeSection::set);

        ConfigurationSection skillSection = classInformationSection.createSection("skill");
        classInformation.mapSkillLevels().forEach(skillSection::set);

        ConfigurationSection skillTreePointsSection = classInformationSection.createSection("skill-tree-points");
        classInformation.mapSkillTreePoints().forEach(skillTreePointsSection::set);

        ConfigurationSection nodeLevelsSection = classInformationSection.createSection("node-levels");
        classInformation.getNodeLevels().forEach(nodeLevelsSection::set);

        ConfigurationSection nodeTimesClaimedSection = classInformationSection.createSection("node-times-claimed");
        classInformation.getNodeTimesClaimed().forEach(nodeTimesClaimedSection::set);

        ConfigurationSection boundSkillsSection = classInformationSection.createSection("bound-skills");
        classInformation.mapBoundSkills().forEach((key, value) -> boundSkillsSection.set(key.toString(), value));

        classInformationSection.set("unlocked-items", new ArrayList<>(classInformation.getUnlockedItems()));
        if (location) {
            RPGProfiles.debug("Saving Profile Location: " + id + " " + index + " " + internalPlayer.getPlayer().getName());

            saveLocation();

        }
        internalPlayer.saveConfig();

        if (balance){
            profileBalance.saveBalance(true);
        }
        if (inventory) {
            RPGProfiles.debug("Saving Profile Inventory: " + id + " " + index + " " + internalPlayer.getPlayer().getName());

            saveInventory();
            if (hasSavedInventory()){
                RPGProfiles.debug("Saved Inventory was successful!");
            }

        }

        created = true;
    }

    public void loadInventory(){
        RPGProfiles.getInventoryManager().loadInventory(internalPlayer.getPlayer(), index);
    }
    public void saveInventory(){
        RPGProfiles.getInventoryManager().saveInventory(getInternalPlayer().getPlayer(), index);
    }
    boolean hasSavedInventory() {
        return RPGProfiles.getInventoryManager().hasSavedInventory(internalPlayer.getPlayer(),index);
    }

    // Method to load location from the configuration
    private Location loadLocation() {
        if (section.isConfigurationSection("location")) {
            ConfigurationSection locSection = section.getConfigurationSection("location");
            return new Location(
                    Bukkit.getWorld(locSection.getString("world")),
                    locSection.getDouble("x"),
                    locSection.getDouble("y"),
                    locSection.getDouble("z"),
                    (float) locSection.getDouble("yaw",0.5),
                    (float) locSection.getDouble("pitch",0.5)
            );
        }
        return null;
    }
    // Method to save location and balance to the configuration
    public void saveLocation() {
        Player player = Bukkit.getPlayer(internalPlayer.getUuid());
        if (player == null){
            player = Bukkit.getOfflinePlayer(internalPlayer.getUuid()).getPlayer();
        }
        if (player == null){
            throw new RuntimeException("Error with Profile owner: " + internalPlayer.getUuid());
        }
        Location loc =  lastKnownLocation==null?player.getLocation():lastKnownLocation;

        ConfigurationSection locSection = section.createSection("location");
        locSection.set("world", loc.getWorld().getName());
        locSection.set("x", loc.getX());
        locSection.set("y", loc.getY());
        locSection.set("z", loc.getZ());
        locSection.set("yaw", loc.getYaw());
        locSection.set("pitch", loc.getPitch());

        internalPlayer.saveConfig();
    }
    @NotNull(value = "Empty Name")
    public String getName() {
        return StringUtils.capitalize(id);
    }

    public ProfileIcon getIcon(){
        ItemStack classIcon = RPGProfiles.getIcons(internalPlayer.getPlayer()).getClassIcon(className);
        return new ProfileIcon(classIcon.getType(),classIcon.getItemMeta().getCustomModelData(),classIcon.getLore().toArray(new String[0]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Profile profile)) return false;

        return new EqualsBuilder().append(getIndex(), profile.getIndex()).append(getId(), profile.getId()).append(getClassName(), profile.getClassName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).append(getClassName()).append(getIndex()).toHashCode();
    }

    public int getIndex() {
        return index;
    }

    public class ProfileIcon{
        private Material material = Material.PAPER;
        private int customModel = 0;
        private final String[] lore;
        ProfileIcon(Material material, int customModel, String[] loreArray){
            this.material = material;
            this.customModel = customModel;
            this.lore = loreArray;
        }
        public ItemStack getItemStack(){
            ItemBuilder builder = new ItemBuilder(material, getName());
            builder.setLore(this.lore);
            builder.editMeta(itemMeta -> itemMeta.setCustomModelData(customModel));
            return builder.asOne();
        }
    }


    public boolean hasSaveInConfig(){
        return section != null && created ;
    }
}
