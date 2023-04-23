package com.profilesplus;

import io.lumine.mythic.bukkit.utils.lib.lang3.StringUtils;
import io.lumine.mythic.lib.api.explorer.ItemBuilder;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class IconsManager {
    private final @NotNull FileConfiguration config = ProfilesPlus.getInstance().getConfig();
    private final @NotNull ItemStack name;
    private final @NotNull ItemStack className;
    private final @NotNull ItemStack locked;
    private final @NotNull ItemStack available;
    private final @NotNull ItemStack cancel;
    private final @NotNull ItemStack confirm;
    private final @NotNull ItemStack back;
    private final @NotNull ItemStack confirmDisabled;
    private final Player player;

    public IconsManager(Player player) {
        this.player = player;

        name = createCustomizedIcon("icons.name", "Set Name", "Assign the name of the Profile!");
        className = createCustomizedIcon("icons.class", "Set Class Type", "Assign the class type of the Profile");
        locked = createCustomizedIcon("icons.locked", "Locked Slot", "");
        available = createCustomizedIcon("icons.available", "Available Slot", "");
        cancel = createCustomizedIcon("icons.cancel", "Cancel", "Cancel the current action");
        confirm = createCustomizedIcon("icons.confirm", "Confirm", "Confirm the current action");
        back = createCustomizedIcon("icons.back", "Back", "Go back to the previous menu");
        confirmDisabled = createCustomizedIcon("icons.confirmDisabled", "Confirm", "Please select a class type before confirming.");
    }
    public ItemStack getClassIcon(String className) {
        String materialName = ProfilesPlus.getInstance().getConfig().getString("icons." + className + ".material");
        int m = ProfilesPlus.getInstance().getConfig().getInt("icons." + className + ".customModel", 0);
        List<String> lore = new ArrayList<>();
        if (ProfilesPlus.getInstance().getConfig().isList("icons." + className + ".lore")) {
            lore = ProfilesPlus.getInstance().getConfig().getStringList("icons." + className + ".lore");
        }

        if (materialName == null) {
            materialName = Material.PAPER.name();
        }
        Material matched = Material.matchMaterial(materialName);

        if (matched == null) {
            matched = Material.PAPER;
        }
        ItemBuilder builder = new ItemBuilder(matched, StringUtils.capitalize(className));
        builder.editMeta(itemMeta -> itemMeta.setCustomModelData(m));
        List<String> finalLore = lore.stream()
                .map(line -> PlaceholderAPI.setPlaceholders(player, line))
                .collect(Collectors.toList());

        if (!finalLore.isEmpty()) {
            builder.setLore(finalLore);
            return builder;
        }
        return builder.asOne();
    }

    private ItemStack createCustomizedIcon(String configPath, String defaultName, String defaultLore) {
        String materialName = config.getString(configPath + ".material", Material.PAPER.name());
        String iconName = config.getString(configPath + ".name", defaultName);
        Material matchedMaterial = Material.matchMaterial(materialName);

        if (matchedMaterial == null) {
            matchedMaterial = Material.PAPER;
        }

        ItemBuilder itemBuilder = new ItemBuilder(matchedMaterial, PlaceholderAPI.setPlaceholders(player, iconName));
        itemBuilder.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt(configPath + ".customModel", 0)));

        List<String> lore = config.getStringList(configPath + ".lore");
        if (lore.isEmpty()) {
            lore.add(defaultLore);
        }

        List<Component> components = new ArrayList<>();
        for (String s : lore) {
            TextComponent component = Component.text(PlaceholderAPI.setPlaceholders(player, s));
            components.add(component);
        }
        itemBuilder.lore(components);

        return itemBuilder.asOne();
    }
    public boolean hasClassIcon(String id) {
        return ProfilesPlus.getInstance().getConfig().isConfigurationSection("icons." + id) && !ProfilesPlus.getInstance().getConfig().getConfigurationSection("icons." + id).getKeys(false).isEmpty();
    }
}
