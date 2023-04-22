package com.profilesplus;

import io.lumine.mythic.lib.api.explorer.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.player.DefaultPlayerData;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
public class Profile {
    private final String displayName;
    private final net.Indyuce.mmocore.api.player.PlayerData mmoCorePlayer;
    private final PlayerData internalPlayer;
    private final SavedClassInformation classInformation;
    private final String className;
    private ConfigurationSection section;
    private final long creationTime;
    private boolean created = false;

    public Profile(String displayName,String className, UUID player){
        this.displayName = displayName;
        this.className = className;
        this.mmoCorePlayer = net.Indyuce.mmocore.api.player.PlayerData.get(player);
        this.internalPlayer = PlayerData.get(player);
        section = internalPlayer.getConfig().getConfigurationSection("profiles." + displayName);
        if (section == null){
            section = internalPlayer.getConfig().createSection("profiles." + displayName);
        }
        if (!section.isConfigurationSection(className)){
            savePlayersInformation();
        }
        this.classInformation = new SavedClassInformation(section.isConfigurationSection( className)?section.getConfigurationSection(className):section.createSection(className));
        this.creationTime = section.isLong("creation-time")?section.getLong("creation-time",System.currentTimeMillis()):System.currentTimeMillis();
        assignName();
    }
    public Profile(String displayName, String className, Player player) {
        this(displayName,className,player.getUniqueId());
    }

    private void assignName(){
        internalPlayer.getPlayer().displayName(Component.text(StringUtils.capitalize(displayName)));
    }

    public void savePlayersInformation() {
        ConfigurationSection config = section.isConfigurationSection("class")? section.getConfigurationSection("class"):section.createSection("class");

        if (config == null){
            throw new RuntimeException("Section not found in " + displayName + " [class]");
        }
        config.set("level", mmoCorePlayer.getLevel());
        config.set("experience", mmoCorePlayer.getExperience());
        config.set("skill-points", mmoCorePlayer.getSkillPoints());
        config.set("attribute-points", mmoCorePlayer.getAttributePoints());
        config.set("attribute-realloc-points", mmoCorePlayer.getAttributeReallocationPoints());
        config.set("skill-reallocation-points", mmoCorePlayer.getSkillReallocationPoints());
        config.set("skill-tree-reallocation-points", mmoCorePlayer.getSkillTreeReallocationPoints());
        config.set("health", mmoCorePlayer.getHealth());
        config.set("mana", mmoCorePlayer.getMana());
        config.set("stamina", mmoCorePlayer.getStamina());
        config.set("stellium", mmoCorePlayer.getStellium());

        ConfigurationSection attributeSection = config.createSection("attribute");
        mmoCorePlayer.getAttributes().mapPoints().forEach(attributeSection::set);

        ConfigurationSection skillSection = config.createSection("skill");
        mmoCorePlayer.mapSkillLevels().forEach(skillSection::set);

        ConfigurationSection skillTreePointsSection = config.createSection("skill-tree-points");
        mmoCorePlayer.mapSkillTreePoints().forEach(skillTreePointsSection::set);

        ConfigurationSection nodeLevelsSection = config.createSection("node-levels");
        mmoCorePlayer.getNodeLevels().forEach(nodeLevelsSection::set);

        ConfigurationSection nodeTimesClaimedSection = config.createSection("node-times-claimed");
        mmoCorePlayer.getNodeTimesClaimed().forEach(nodeTimesClaimedSection::set);

        ConfigurationSection boundSkillsSection = config.createSection("bound-skills");
        mmoCorePlayer.mapBoundSkills().forEach((key, value) -> boundSkillsSection.set(key.toString(), value));

        config.set("unlocked-items", new ArrayList<>(mmoCorePlayer.getUnlockedItems()));

        section.set("creation-time",System.currentTimeMillis());
        internalPlayer.saveConfig();

        created = true;
    }


    @NotNull(exception = RuntimeException.class,value = "Empty Profile Name!")
    public String getName() {
        return StringUtils.capitalize(displayName);
    }

    public ProfileIcon getIcon(){
        String materialName = ProfilesPlus.getInstance().getConfig().getString("icon." + className + ".material");
        int m = ProfilesPlus.getInstance().getConfig().getInt("icon." + className + ".customModel", 0);
        Material matched = Material.matchMaterial(materialName);

        if (matched == null){
            matched = Material.PAPER;
        }
        return new ProfileIcon(matched,m);
    }
    public class ProfileIcon{
        private Material material = Material.PAPER;
        private int customModel = 0;
        private List<String> lore = new ArrayList<>();
        ProfileIcon(Material material, int customModel){
            this.material = material;
            this.customModel = customModel;
            if (this.lore.isEmpty()) {
                this.lore.add("Name: " + getName());
                this.lore.add("Class:" + MMOCore.plugin.classManager.getOrThrow(getClassName()).getName());
                this.lore.add("Level: " + classInformation.getLevel());
                this.lore.add("Health: " + classInformation.getHealth());
            }
        }
        public ItemStack getItemStack(){
            ItemBuilder builder = new ItemBuilder(material, getName());
            builder.setLore(this.lore.toArray(value -> lore.toArray(new String[0])));
            builder.editMeta(itemMeta -> itemMeta.setCustomModelData(customModel));
            return builder.asOne();
        }
    }
}
