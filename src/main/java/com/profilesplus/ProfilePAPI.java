package com.profilesplus;

import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
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
        if (parts.length < 3) {
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
        PlayerData playerData = PlayerData.get(player.getUniqueId());
        Profile profile = playerData.getProfiles().get(slot);
        if (profile == null) {
            return null;
        }

        // Check which stat type is being requested
        if ("attributes".equals(statType)) {
            // Get the attribute level
            for (PlayerAttribute playerAttribute : MMOCore.plugin.attributeManager.getAll()) {
                if (playerAttribute.getId().toString().equalsIgnoreCase(statOrAttribute)) {
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
