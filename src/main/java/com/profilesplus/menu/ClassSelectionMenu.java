package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSelectionMenu extends ConfirmCancelMenu {
    private final ProfileCreateMenu createMenu;
    private String selectedClassType;

    public ClassSelectionMenu(Player player, ProfileCreateMenu createMenu) {
        super(player, createMenu.plugin, "Select Class Type", 27);
        this.createMenu = createMenu;
        Map<String, ItemStack> classIcons = new HashMap<>();

        for (PlayerClass aClass : MMOCore.plugin.classManager.getAll()) {
            if (ProfilesPlus.getIcons(player).hasClassIcon(aClass.getId())) {
                classIcons.put(aClass.getId(), ProfilesPlus.getIcons(player).getClassIcon(aClass.getId()));
            }
        }

        int[] centerSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        int index = 0;

        for (Map.Entry<String, ItemStack> classIconEntry : classIcons.entrySet()) {
            if (index >= centerSlots.length) {
                break;
            }

            int slot = centerSlots[index++];
            ItemStack classIcon = classIconEntry.getValue();
            String classType = classIconEntry.getKey();

            setItem(slot, classIcon, event -> {
                selectedClassType = classType;
                for (int i : centerSlots) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null) {
                        item.removeEnchantment(org.bukkit.enchantments.Enchantment.ARROW_DAMAGE);
                    }
                }
                classIcon.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.ARROW_DAMAGE, 1);
                updateConfirmButtonAppearance();
            });
        }
    }

    @Override
    protected boolean canConfirm() {
        return selectedClassType != null;
    }

    @Override
    protected String failedConfirmMessage() {
        return "";
    }

    @Override
    protected String successfulConfirmMessage() {
        return "";
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        // Set the class type in the ProfileCreateMenu
        createMenu.setClassType(selectedClassType);

        //Include a way to update the lore of the items here!
        //todo Fix this so the display of the lore input works!

        // Close the ClassSelectionMenu and return to the ProfileCreateMenu
        player.closeInventory();
        createMenu.open();
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        // Close the ClassSelectionMenu and return to the ProfileCreateMenu
        player.closeInventory();
        createMenu.open();
    }

    @Override
    public List<String> confirmLore() {

        //todo fix lore
        return new ArrayList<>();

    }


    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
