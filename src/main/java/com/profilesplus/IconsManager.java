package com.profilesplus;

import io.lumine.mythic.lib.api.explorer.ItemBuilder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


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

    public IconsManager() {
        String materialName = config.getString("icons.name.material", Material.PAPER.name());
        name = new ItemBuilder(Material.matchMaterial(materialName), "Set Name").asOne();
        name.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.name.customModel",0)));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Assign the name of the Profile!");
        name.setLore(lore);
        lore.clear();

        String classMaterialName = config.getString("icons.class.material", Material.SHIELD.name());
        className = new ItemBuilder(Material.matchMaterial(classMaterialName), "Set Class Type").asOne();
        className.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.class.customModel",0)));
        lore.add("Assign the class type of the Profile");
        className.setLore(lore);
        lore.clear();

        String lockedType = config.getString("icons.locked.material", Material.RED_STAINED_GLASS_PANE.name());
        locked = new ItemBuilder(Material.matchMaterial(lockedType), "Locked Slot").asOne();
        locked.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.locked.customModel",0)));
        lore.add("");
        locked.setLore(lore);
        lore.clear();

        lockedType = config.getString("icons.available.material", Material.GREEN_STAINED_GLASS_PANE.name());
        available = new ItemBuilder(Material.matchMaterial(lockedType), "Available Slot").asOne();
        available.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.available.customModel",0)));
        lore.add("");
        available.setLore(lore);
        lore.clear();

        String cancelMaterialName = config.getString("icons.cancel.material", Material.BARRIER.name());
        cancel = new ItemBuilder(Material.matchMaterial(cancelMaterialName), "Cancel").asOne();
        cancel.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.cancel.customModel", 0)));
        ArrayList<String> cancelLore = new ArrayList<>();
        cancelLore.add("Cancel the current action");
        cancel.setLore(cancelLore);

        String confirmMaterialName = config.getString("icons.confirm.material", Material.EMERALD_BLOCK.name());
        confirm = new ItemBuilder(Material.matchMaterial(confirmMaterialName), "Confirm").asOne();
        confirm.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.confirm.customModel", 0)));
        ArrayList<String> confirmLore = new ArrayList<>();
        confirmLore.add("Confirm the current action");
        confirm.setLore(confirmLore);

        String backMaterialName = config.getString("icons.back.material", Material.ARROW.name());
        back = new ItemBuilder(Material.matchMaterial(backMaterialName), "Back").asOne();
        back.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.back.customModel", 0)));
        ArrayList<String> backLore = new ArrayList<>();
        backLore.add("Go back to the previous menu");
        back.setLore(backLore);

        String confirmDisabledMaterialName = config.getString("icons.confirmDisabled.material", Material.REDSTONE_BLOCK.name());
        confirmDisabled = new ItemBuilder(Material.matchMaterial(confirmDisabledMaterialName), "Confirm").asOne();
        confirmDisabled.editMeta(itemMeta -> itemMeta.setCustomModelData(config.getInt("icons.confirmDisabled.customModel", 0)));
        ArrayList<String> confirmDisabledLore = new ArrayList<>();
        confirmDisabledLore.add("Please select a class type before confirming.");
        confirmDisabled.setLore(confirmDisabledLore);
    }

    public ItemStack getClassIcon(String className){
        String materialName = ProfilesPlus.getInstance().getConfig().getString("icons." + className + ".material");
        int m = ProfilesPlus.getInstance().getConfig().getInt("icons." + className + ".customModel", 0);
        List<String> lore = new ArrayList<>();
        if (ProfilesPlus.getInstance().getConfig().isList("icons." + className + ".lore")) {
           lore = ProfilesPlus.getInstance().getConfig().getStringList("icons." + className + ".lore");
        }

        if (materialName == null){
            materialName = Material.PAPER.name();
        }
        Material matched = Material.matchMaterial(materialName);

        if (matched == null){
            matched = Material.PAPER;
        }
        ItemBuilder builder = new ItemBuilder(matched, StringUtils.capitalize(className));
        builder.editMeta(itemMeta -> itemMeta.setCustomModelData(m));
        List<String> finalLore = lore;
        if (!finalLore.isEmpty()) {
            String[] array = lore.toArray(value -> finalLore.toArray(new String[0]));

            return builder.setLore(array).asOne();
        }
        return builder.asOne();
    }

    public boolean hasClassIcon(String id) {
        return ProfilesPlus.getInstance().getConfig().isConfigurationSection("icons." + id) && !ProfilesPlus.getInstance().getConfig().getConfigurationSection("icons." + id).getKeys(false).isEmpty();
    }
}
