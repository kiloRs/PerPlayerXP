package com.profilesplus;

import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ProfilePAPI extends PlaceholderExpansion {
    private final RPGProfiles plugin;

    public ProfilePAPI(RPGProfiles plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "RPGProfiles";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // Split the identifier by '_'
        String[] parts = identifier.split("_");

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        if (parts.length == 2 && !NumberUtils.isDigits(parts[0])){
            String active = parts[0];
            if (active.equalsIgnoreCase("active")){
                return String.valueOf(playerData.getProfileStorage().getActiveProfile().getIndex());
            }
        }
        if (parts.length == 2){
            int slot = Integer.parseInt(parts[0]);
            String exists = parts[1];
            if ("exists".equalsIgnoreCase(exists)) {
                Profile profile = playerData.getProfileStorage().get(slot);
                return profile != null && profile.isCreated() ? "True" : "False";
            }
            else if ("name".equalsIgnoreCase(exists)){
                Profile profile = playerData.getProfileStorage().get(slot);
                return profile.getId();
            }
            else if ("index".equalsIgnoreCase(exists)){
                Profile profile = playerData.getProfileStorage().get(slot);
                return String.valueOf(profile.getIndex());
            }
            else if ("active".equalsIgnoreCase(exists)){
                return playerData.getProfileStorage().hasActiveProfile() && playerData.getProfileStorage().isActiveProfile(slot)?"True":"False";
            }
            return null;
        }
        // Get the slot number and stat type
        int slot;
        try {
            slot = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        String statType = parts[1];
        String statOrAttribute = parts[2];

        // Get player profile by slot
        Profile profile = playerData.getProfileStorage().get(slot);
        if (profile == null) {
            return null;
        }

        // Check which stat type is being requested
        if ("attributes".equals(statType)) {
            // Get the attribute level
            for (PlayerAttribute playerAttribute : MMOCore.plugin.attributeManager.getAll()) {
                if (playerAttribute.getId().equalsIgnoreCase(statOrAttribute)) {
                    return Integer.toString(profile.getClassInformation().getAttributeLevel(playerAttribute.getId()));
                }
            }
        } else if ("stats".equals(statType)) {
            // Check which stat is being requested and return the value
            return switch (statOrAttribute) {
                case "name" -> profile.getName();
                case "class" -> profile.getClassName();
                case "level" -> Integer.toString(profile.getClassInformation().getLevel());
                case "health" -> Double.toString(profile.getClassInformation().getHealth());
                case "experience" -> Double.toString(profile.getClassInformation().getExperience());
                case "stamina" -> Double.toString(profile.getClassInformation().getStamina());
                case "mana" -> Double.toString(profile.getClassInformation().getMana());
                case "stellium" -> Double.toString(profile.getClassInformation().getStellium());
                case "skill_points" -> Integer.toString(profile.getClassInformation().getSkillPoints());
                case "attribute_points" -> Integer.toString(profile.getClassInformation().getAttributePoints());
                case "attribute_reallocation_points" ->
                        Integer.toString(profile.getClassInformation().getAttributeReallocationPoints());
                case "skill_reallocation_points" ->
                        Integer.toString(profile.getClassInformation().getSkillReallocationPoints());
                default -> null;
            };
        }
        return null;
    }
}
