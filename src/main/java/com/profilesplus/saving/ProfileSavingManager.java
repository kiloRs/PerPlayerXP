package com.profilesplus.saving;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.Profile;
import com.profilesplus.configs.SavedInformation;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.player.ClassDataContainer;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import net.Indyuce.mmocore.skilltree.tree.SkillTree;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfileSavingManager {


    private SavedInformation defaultData;

    public void loadData(Profile profile) {
        com.profilesplus.players.PlayerData profilePlayer = profile.getInternalPlayer();
        ConfigurationSection section = profile.getSection();
        defaultData = RPGProfiles.getStatHandler().get(profile.getClassName());
        if (section == null){
            throw new RuntimeException("Cannot Init Profile YAML from Section");
        }
        final ConfigurationSection config = section;

        PlayerData data = PlayerData.get(profilePlayer.getUuid());

        // Reset stats linked to triggers.
        data.resetTriggerStats();

//        data.setClassPoints(config.getInt("class-points", getDefaultData().getClassPoints()));
        data.setSkillPoints(config.getInt("skill-points", getDefaultData().getSkillPoints()));
        data.setSkillReallocationPoints(config.getInt("skill-reallocation-points", getDefaultData().getSkillReallocationPoints()));
        data.setSkillTreeReallocationPoints(config.getInt("skill-tree-reallocation-points", getDefaultData().getSkillTreeReallocationPoints()));
        data.setAttributePoints(config.getInt("attribute-points", getDefaultData().getAttributePoints()));
        data.setAttributeReallocationPoints(config.getInt("attribute-realloc-points", getDefaultData().getAttributeReallocationPoints()));
        data.setLevel(config.getInt("level", getDefaultData().getLevel()));
        data.setExperience(config.getInt("experience"));
        if (config.contains("class"))
            data.setClass(MMOCore.plugin.classManager.get(config.getString("class")));

        if (config.contains("guild")) {
            Guild guild = MMOCore.plugin.dataProvider.getGuildManager().getGuild(config.getString("guild"));
            if (guild != null) {
                data.setGuild(guild.hasMember(data.getUniqueId()) ? guild : null);
            }
        }
        if (config.contains("attribute"))
            data.getAttributes().load(config.getConfigurationSection("attribute"));
        if (config.contains("profession"))
            data.getCollectionSkills().load(config.getConfigurationSection("profession"));
        if (config.contains("quest"))
            data.getQuestData().load(config.getConfigurationSection("quest"));
        data.getQuestData().updateBossBar();
        if (config.contains("waypoints"))
            data.getWaypoints().addAll(config.getStringList("waypoints"));
        if (config.contains("friends"))
            config.getStringList("friends").forEach(str -> data.getFriends().add(UUID.fromString(str)));
        if (config.contains("skill"))
            config.getConfigurationSection("skill").getKeys(false).forEach(id -> data.setSkillLevel(id, config.getInt("skill." + id)));
        if (config.isConfigurationSection("bound-skills"))
            for (String key : config.getConfigurationSection("bound-skills").getKeys(false)) {
                ClassSkill skill = data.getProfess().getSkill(config.getString("bound-skills." + key));
                data.bindSkill(Integer.parseInt(key), skill);
            }

        for (String key : MMOCore.plugin.skillTreeManager.getAll().
                stream().
                map(SkillTree::getId).
                toList()) {
            data.setSkillTreePoints(key, config.getInt("skill-tree-points." + key, 0));
        }
        data.setSkillTreePoints("global", config.getInt("skill-tree-points.global", 0));

        if (config.contains("times-claimed"))
            for (String key : config.getConfigurationSection("times-claimed").getKeys(false)) {
                ConfigurationSection sub= config.getConfigurationSection("times-claimed." + key);
                if (sub != null)
                    for (String key1 : sub.getKeys(false)) {
                        ConfigurationSection sub1 = section.getConfigurationSection(key1);
                        if (sub1 != null)
                            for (String key2 : config.getConfigurationSection("times-claimed." + key + "." + key1).getKeys(false)) {
                                data.getItemClaims().put(key + "." + key1 + "." + key2, config.getInt("times-claimed." + key + "." + key1 + "." + key2));

                            }
                    }
            }

        data.setUnlockedItems(new HashSet<>(config.getStringList("unlocked-items")));
        for (SkillTreeNode node : MMOCore.plugin.skillTreeManager.getAllNodes()) {
            if (config.contains("skill-tree-level." + node.getFullId())) {
                data.setNodeLevel(node, config.getInt("skill-tree-level." + node.getFullId(), 0));
            }
        }
        data.setupSkillTree();

//        // Load class slots, use try so the player can log in.
//        if (config.contains("class-info"))
//            for (
//                    String key : config.getConfigurationSection("class-info").
//
//                    getKeys(false))
//                try {
//                    PlayerClass profess = MMOCore.plugin.classManager.get(key);
//                    Validate.notNull(profess, "Could not find class '" + key + "'");
//                    data.applyClassInfo(profess, new SavedClassInformation(config.getConfigurationSection("class-info." + key)));
//                } catch (
//                        IllegalArgumentException exception) {
//                    MMOCore.log(Level.WARNING, "Could not load class info '" + key + "': " + exception.getMessage());
//                }

        /*
         * These should be loaded after to make sure that the
         * MAX_MANA, MAX_STAMINA & MAX_STELLIUM stats are already loaded.
         */
        data.setHealth(config.getDouble("health", defaultData.getHealth()));
        data.setMana(config.contains("mana") ? config.getDouble("mana") : data.getStats().getStat("MAX_MANA"));
        data.setStamina(config.contains("stamina") ? config.getDouble("stamina") : data.getStats().getStat("MAX_STAMINA"));
        data.setStellium(config.contains("stellium") ? config.getDouble("stellium") : data.getStats().getStat("MAX_STELLIUM"));

        if (data.isOnline())
            data.getPlayer().setHealth(MMOCoreUtils.fixResource(config.getDouble("health"), data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));

        data.setFullyLoaded();
        profile.setLoaded(true);
    }

    public void saveData(Profile profile, boolean fromPlayer) {
        com.profilesplus.players.PlayerData profilePlayer = profile.getInternalPlayer();
        @Nullable ConfigurationSection section = profilePlayer.getConfig().getConfigurationSection("profiles." + profile.getIndex());
        PlayerData data = PlayerData.get(profilePlayer.getUuid());
        if (section == null){
            section = profilePlayer.getConfig().createSection("profiles." + profile.getIndex());
        }
        if (fromPlayer) {
            final ConfigurationSection config = section;
            if (!config.isLong("creation-time")){
                config.set("creation-time",System.currentTimeMillis());
            }
            config.set("saves",config.getInt("saves",0) + 1);
//            config.set("class-points", data.getClassPoints());
            config.set("skill-points", data.getSkillPoints());
            config.set("skill-reallocation-points", data.getSkillReallocationPoints());
            config.set("attribute-points", data.getAttributePoints());
            // config.set("skill-realloc-points", skillReallocationPoints);
            config.set("attribute-realloc-points", data.getAttributeReallocationPoints());
            config.set("level", data.getLevel());
            config.set("experience", data.getExperience());
            config.set("class", data.getProfess().getId());
            config.set("waypoints", new ArrayList<>(data.getWaypoints()));
            config.set("friends", data.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
            config.set("last-login", data.getLastLogin());
            config.set("guild", data.hasGuild() ? data.getGuild().getId() : null);
            data.mapSkillTreePoints().forEach((key1, value) -> config.set("skill-tree-points." + key1, value));
            config.set("skill-tree-reallocation-points", data.getSkillTreeReallocationPoints());
            config.set("skill", null);
            config.set("health", data.getHealth());
            config.set("mana", data.getMana());
            config.set("stellium", data.getStellium());
            config.set("stamina", data.getStamina());
            //Saves the nodes levels
            MMOCore.plugin.skillTreeManager.getAllNodes().forEach(node -> config.set("skill-tree-level." + node.getFullId(), data.getNodeLevel(node)));
            data.mapSkillLevels().forEach((key1, value) -> config.set("skill." + key1, value));
            data.getItemClaims().forEach((key, times) -> config.set("times-claimed." + key, times));

            config.set("bound-skills", null);
            data.mapBoundSkills().forEach((slot, skill) -> config.set("bound-skills." + slot, skill));
            config.set("unlocked-items", new ArrayList<>(data.getUnlockedItems()));
            config.set("attribute", null);
            config.createSection("attribute");
            data.getAttributes().save(config.getConfigurationSection("attribute"));

            config.set("profession", null);
            config.createSection("profession");
            data.getCollectionSkills().save(config.getConfigurationSection("profession"));

            config.set("quest", null);
            config.createSection("quest");
            data.getQuestData().save(config.getConfigurationSection("quest"));

            config.set("location",null);
            @NotNull Map<String, Object> locationKeys = profilePlayer.getPlayer().getLocation().serialize();
            config.createSection("location", locationKeys);;
            config.set("balance",RPGProfiles.getEconomy().getBalance(profilePlayer.getPlayer()));
//            config.set("class-info", null);
//            for (String key : data.getSavedClasses()) {
//                SavedClassInformation info = data.getClassInfo(key);
//                config.set("class-info." + key + ".level", info.getLevel());
//                config.set("class-info." + key + ".experience", info.getExperience());
//                config.set("class-info." + key + ".skill-points", info.getSkillPoints());
//                config.set("class-info." + key + ".attribute-points", info.getAttributePoints());
//                config.set("class-info." + key + ".attribute-realloc-points", info.getAttributeReallocationPoints());
//                config.set("class-info." + key + ".skill-tree-reallocation-points", info.getSkillTreeReallocationPoints());
//                config.set("class-info." + key + ".skill-reallocation-points", info.getSkillReallocationPoints());
//                config.set("class-info." + key + ".health", info.getHealth());
//                config.set("class-info." + key + ".mana", info.getMana());
//                config.set("class-info." + key + ".stamina", info.getStamina());
//                config.set("class-info." + key + ".stellium", info.getStellium());
//                info.getSkillKeys().forEach(skill -> config.set("class-info." + key + ".skill." + skill, info.getSkillLevel(skill)));
//                info.getAttributeKeys().forEach(attribute -> config.set("class-info." + key + ".attribute." + attribute, info.getAttributeLevel(attribute)));
//                info.getNodeKeys().forEach(node -> config.set("class-info." + key + ".node-levels." + node, info.getNodeLevel(node)));
//                info.getSkillTreePointsKeys().forEach(skillTreeId -> config.set("class-info." + key + ".skill-tree-points." + skillTreeId, info.getAttributeLevel(skillTreeId)));
//                info.mapBoundSkills().forEach((slot, skill) -> config.set("class-info." + key + ".bound-skills." + slot, skill));
//                config.set("class-info." + key + ".unlocked-items", new ArrayList<>(info.getUnlockedItems()));
//            }
        }
        else {
            final ConfigurationSection config = section;
            SavedInformation information = RPGProfiles.getStatHandler().get(profile.getClassName());
//            config.set("class-points", data.getClassPoints());
            config.set("skill-points", information.getSkillPoints());
            config.set("skill-reallocation-points", information.getSkillReallocationPoints());
            config.set("attribute-points", information.getAttributePoints());
            // config.set("skill-realloc-points", skillReallocationPoints);
            config.set("attribute-realloc-points", information.getAttributeReallocationPoints());
            config.set("level", information.getLevel());
            config.set("experience",information.getExperience());
            config.set("class", profile.getClassName().toUpperCase());
            config.set("waypoints", new ArrayList<>());
            information.mapSkillTreePoints().forEach((key1, value) -> config.set("skill-tree-points." + key1, value));
            config.set("skill-tree-reallocation-points", information.getSkillTreeReallocationPoints());
            config.set("skill", null);
            config.set("health", information.getHealth());
            config.set("mana", information.getMana());
            config.set("stellium", information.getStellium());
            config.set("stamina", information.getStamina());
            //Saves the nodes levels
            MMOCore.plugin.skillTreeManager.getAllNodes().forEach(node -> config.set("skill-tree-level." + node.getFullId(), information.getNodeLevel(node.getFullId())));
            information.mapSkillLevels().forEach((key1, value) -> config.set("skill." + key1, value));
            config.set("bound-skills", null);
            information.mapBoundSkills().forEach((slot, skill) -> config.set("bound-skills." + slot, skill));
            config.set("unlocked-items", new ArrayList<>(information.getUnlockedItems()));
            config.set("location",null);
            config.createSection("location",information.getLocation().serialize());
            config.set("balance",information.getBalance());

//            config.set("attribute", null);
//            config.createSection("attribute");
//            config.set("profession", null);
//            config.createSection("profession");
//            data.getCollectionSkills().save(config.getConfigurationSection("profession"));
//
//            config.set("quest", null);
//            config.createSection("quest");
//            data.getQuestData().save(config.getConfigurationSection("quest"));

        }
        try {
            profilePlayer.getConfig().save(profilePlayer.getPlayerFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveStarting(PlayerClass playerClass){
        String path = playerClass.getId().toUpperCase();
        final ConfigurationSection config = RPGProfiles.getDefaultPlayerConfig().isConfigurationSection(path)?RPGProfiles.getDefaultPlayerConfig().getConfigurationSection(path):RPGProfiles.getDefaultPlayerConfig().createSection(path);
//            config.set("class-points", data.getClassPoints());
        if (config == null){
            throw new RuntimeException("Class Starting Config Error");
        }
        config.set("skill-points", 0);
        config.set("skill-reallocation-points", 0);
        config.set("attribute-points", 0);
        config.set("attribute-realloc-points",0);
        config.set("level", 1);
        config.set("experience",0);
        config.set("class", playerClass.getId());
        config.set("skill-tree-reallocation-points",0);
        config.set("health", 20);
        config.set("mana", 0);
        config.set("stellium", 0);
        config.set("stamina", 0);
        //Saves the nodes levels
        config.set("bound-skills", null);
        config.set("unlocked-items", null);

        try {
            File file = new File(RPGProfiles.getInstance().getDataFolder(), "defaultClassValues.yml");
            if (!file.exists() || !file.isFile()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            RPGProfiles.getDefaultPlayerConfig().save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public ClassDataContainer getDefaultData() {
        return defaultData;
    }

    public void saveInventory(Profile profile) {
        RPGProfiles.getInventoryManager().saveInventory(profile.getInternalPlayer().getPlayer(), profile.getIndex());
    }
    public void loadInventory(Profile profile){
        RPGProfiles.getInventoryManager().loadInventory(profile.getInternalPlayer().getPlayer(), profile.getIndex());
    }
}
