package com.profilesplus.players;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.saving.BukkitSerialization;
import io.lumine.mythic.lib.api.explorer.ItemBuilder;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
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
import java.util.List;
import java.util.UUID;


@Getter
public class Profile {
    private final String id;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCorePlayer;
    private final PlayerData internalPlayer;
    private final SavedClassInformation classInformation;
    private final String className;
    private ConfigurationSection section;
    private final long creationTime;
    private boolean created = false;
    private double balance = 0;
    private Location lastKnownLocation;
    private final int index;

    public Profile(String id, String className,int slotNumber, UUID player){
        this.id = id;
        this.index = slotNumber;
        this.className = className;
        this.mmoCorePlayer = net.Indyuce.mmocore.api.player.PlayerData.get(player);
        this.internalPlayer = PlayerData.get(player);
        section = internalPlayer.getConfig().getConfigurationSection("profiles." + this.index);
        if (section == null){
            section = internalPlayer.getConfig().createSection("profiles." + this.index);
        }
        this.classInformation = new SavedClassInformation(section.isConfigurationSection(className)?section.getConfigurationSection(className):section.createSection(className));
        this.creationTime = section.isLong("creation-time")?section.getLong("creation-time",System.currentTimeMillis()):System.currentTimeMillis();
        assignName();


        this.lastKnownLocation = loadLocation();

        if (this.lastKnownLocation == null){
            lastKnownLocation = internalPlayer.getPlayer().getLocation();
        }
        if (ProfilesPlus.isUsingEconomy()){
            if (section.isDouble("balance")){
                balance = section.getDouble("balance");
            }
        }
        else {
            balance = 0;
        }

    }
    public Profile(String id, String className, int slot, Player player) {
        this(id,className,slot,player.getUniqueId());
    }

    private void assignName() {
        String name = StringUtils.capitalize(id);
        internalPlayer.getPlayer().displayName(Component.text(name));
        internalPlayer.getPlayer().playerListName(Component.text(name));

    }

    public void update() {
        // Save the current active profile if it exists
        PlayerData playerData = PlayerData.get(internalPlayer.getUuid());

        if (playerData.getActiveProfile() != null) {
//        if (playerData.getActiveProfile() != null){
//            playerData.getActiveProfile().saveToConfigurationSection();
//            ProfilesPlus.getInventoryDatabase().saveInventory(internalPlayer.getUuid().toString(), playerData.getActiveSlot(), BukkitSerialization.serializeInventory(playerData.getPlayer().getInventory()));
//        }

            if (!playerData.getActiveProfile().id.equalsIgnoreCase(this.id)) {
                playerData.setActiveProfile(this.index);
            }
        }
        else {
            playerData.setActiveProfile(this.index);
        }
        new BukkitRunnable(){
            @Override
            public void run() {

                saveToConfigurationSection();

                updateMMOCore();
                if (lastKnownLocation != null) {
                    internalPlayer.getPlayer().teleport(lastKnownLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
                // Set the player's balance to the last saved balance of the active profile

                internalPlayer.getConfig().set("active",id.toUpperCase());
                internalPlayer.saveConfig();

                loadInventory();

            }
        }.runTask(ProfilesPlus.getInstance());
    }
    private void saveInventory(){
        ProfilesPlus.getInventoryDatabase().saveInventory(internalPlayer.getUuid().toString(), index,BukkitSerialization.serializeInventory(internalPlayer.getPlayer().getInventory()));
    }
    public void loadInventory(){
        ProfilesPlus.getInventoryDatabase().loadInventory(internalPlayer.getUuid().toString(), index);
    }
    private void updateMMOCore(){
        mmoCorePlayer.setClass(MMOCore.plugin.classManager.getOrThrow(className));
        mmoCorePlayer.setExperience(classInformation.getExperience());
        mmoCorePlayer.setLevel(classInformation.getLevel());
        mmoCorePlayer.setHealth(classInformation.getHealth());
        mmoCorePlayer.setStamina(classInformation.getStamina());
        mmoCorePlayer.setMana(classInformation.getMana());
        mmoCorePlayer.setStellium(classInformation.getStellium());
        mmoCorePlayer.setSkillPoints(classInformation.getSkillPoints());
        mmoCorePlayer.setSkillReallocationPoints(classInformation.getSkillReallocationPoints());
        mmoCorePlayer.setAttributePoints(classInformation.getAttributePoints());
        mmoCorePlayer.setAttributeReallocationPoints(classInformation.getAttributeReallocationPoints());
        for (SkillTree skillTree : MMOCore.plugin.skillTreeManager.getAll()) {
            mmoCorePlayer.setSkillTreePoints(skillTree.getId(),classInformation.getSkillTreePoints(skillTree.getId()));
            for (SkillTreeNode node : skillTree.getNodes()) {
                mmoCorePlayer.setNodeLevel(node,classInformation.getNodeLevel(node.getId()));
            }
        }
        for (RegisteredSkill registeredSkill : MMOCore.plugin.skillManager.getAll()) {
            int skillLevel = classInformation.getSkillLevel(registeredSkill);
            if (skillLevel > 0){
                mmoCorePlayer.setSkillLevel(registeredSkill,skillLevel);
            }
        }
        for (PlayerAttribute playerAttribute : MMOCore.plugin.attributeManager.getAll()) {
            int attributeLevel = classInformation.getAttributeLevel(playerAttribute.getId());
            mmoCorePlayer.getAttributes().getInstance(playerAttribute).setBase(attributeLevel);
        }

    }
    public void saveToConfigurationSection() {
        section.set("id", id);
        section.set("className", className);
        if (!section.contains("creation-time")) {
            section.set("creation-time", creationTime);
        }
        // Save classInformation
        ConfigurationSection classInformationSection = section.isConfigurationSection(className) ?
                section.getConfigurationSection(className) : section.createSection(className);


        if (classInformationSection == null){
            throw new RuntimeException("No Class Information in " + id);
        }
        // Save all properties of SavedClassInformation
        classInformationSection.set("level", mmoCorePlayer.getLevel());
        classInformationSection.set("experience", mmoCorePlayer.getExperience());
        classInformationSection.set("skill-points", mmoCorePlayer.getSkillPoints());
        classInformationSection.set("attribute-points", mmoCorePlayer.getAttributePoints());
        classInformationSection.set("attribute-realloc-points", mmoCorePlayer.getAttributeReallocationPoints());
        classInformationSection.set("skill-reallocation-points", mmoCorePlayer.getSkillReallocationPoints());
        classInformationSection.set("skill-tree-reallocation-points", mmoCorePlayer.getSkillTreeReallocationPoints());
        classInformationSection.set("health", mmoCorePlayer.getHealth());
        classInformationSection.set("mana", mmoCorePlayer.getMana());
        classInformationSection.set("stamina", mmoCorePlayer.getStamina());
        classInformationSection.set("stellium", mmoCorePlayer.getStellium());

        ConfigurationSection attributeSection = classInformationSection.createSection("attribute");
        mmoCorePlayer.getAttributes().mapPoints().forEach(attributeSection::set);

        ConfigurationSection skillSection = classInformationSection.createSection("skill");
        mmoCorePlayer.mapSkillLevels().forEach(skillSection::set);

        ConfigurationSection skillTreePointsSection = classInformationSection.createSection("skill-tree-points");
        mmoCorePlayer.mapSkillTreePoints().forEach(skillTreePointsSection::set);

        ConfigurationSection nodeLevelsSection = classInformationSection.createSection("node-levels");
        mmoCorePlayer.getNodeLevels().forEach(nodeLevelsSection::set);

        ConfigurationSection nodeTimesClaimedSection = classInformationSection.createSection("node-times-claimed");
        mmoCorePlayer.getNodeTimesClaimed().forEach(nodeTimesClaimedSection::set);

        ConfigurationSection boundSkillsSection = classInformationSection.createSection("bound-skills");
        mmoCorePlayer.mapBoundSkills().forEach((key, value) -> boundSkillsSection.set(key.toString(), value));

        classInformationSection.set("unlocked-items", new ArrayList<>(mmoCorePlayer.getUnlockedItems()));

        saveLocationAndBalance();

        internalPlayer.saveConfig();

        saveInventory();
        created = true;
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
                    (float) locSection.getDouble("yaw"),
                    (float) locSection.getDouble("pitch")
            );
        }
        return null;
    }

    public void setLastKnownLocation(Location location){
        this.lastKnownLocation = location.toBlockLocation();
    }
    // Method to save location and balance to the configuration
    public void saveLocationAndBalance() {
        Player player = Bukkit.getPlayer(internalPlayer.getUuid());
        if (player == null){
            player = Bukkit.getOfflinePlayer(internalPlayer.getUuid()).getPlayer();
        }
        if (player == null){
            throw new RuntimeException("Error with Profile owner: " + internalPlayer.getUuid());
        }
        Location loc = player.getLocation();

        ConfigurationSection locSection = section.createSection("location");
        locSection.set("world", loc.getWorld().getName());
        locSection.set("x", loc.getX());
        locSection.set("y", loc.getY());
        locSection.set("z", loc.getZ());
        locSection.set("yaw", loc.getYaw());
        locSection.set("pitch", loc.getPitch());

        if (ProfilesPlus.isUsingEconomy()) {
            Economy economy = ((ProfilesPlus) ProfilesPlus.getInstance()).getEconomy();
            balance = economy.getBalance(player);
            section.set("balance", balance);
        }
        internalPlayer.saveConfig();
    }
    @NotNull(value = "Empty Name")
    public String getName() {
        return StringUtils.capitalize(id);
    }

    public ProfileIcon getIcon(){
        String materialName = ProfilesPlus.getInstance().getConfig().getString("icon." + className + ".material");
        int m = ProfilesPlus.getInstance().getConfig().getInt("icon." + className + ".customModel", 0);
        List<String> list = ProfilesPlus.getInstance().getConfig().getStringList("icons." + className + ".lore");
        String[] lore = list.toArray(value -> list.toArray(new String[0]));

        if (lore == null){
            lore = new String[]{"Class Icon is Empty!"};
        }
        if (materialName == null){
            materialName = Material.PAPER.name();
        }
        Material matched = Material.matchMaterial(materialName);

        if (matched == null){
            matched = Material.PAPER;
        }
        return new ProfileIcon(matched,m,lore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Profile profile)) return false;

        return new EqualsBuilder().append(getCreationTime(), profile.getCreationTime()).append(getIndex(), profile.getIndex()).append(getId(), profile.getId()).append(getClassName(), profile.getClassName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).append(getClassName()).append(getCreationTime()).append(getIndex()).toHashCode();
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
}
