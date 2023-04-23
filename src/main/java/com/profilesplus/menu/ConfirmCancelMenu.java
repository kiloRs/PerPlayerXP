package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;


public abstract class ConfirmCancelMenu extends InventoryGUI {
    protected final ItemStack cancelButton;
    protected final ItemStack confirmButton;
    protected final ItemStack confirmDisabledButton;
    private final ConfirmChange confirmChangeType;

    public ConfirmCancelMenu(Player player, Plugin plugin, String title, int size) {
        super(player, plugin, title, size);

        cancelButton = ProfilesPlus.getIcons(player).getCancel();
        confirmButton = ProfilesPlus.getIcons(player).getConfirm();
        confirmDisabledButton = ProfilesPlus.getIcons(player).getConfirmDisabled();

        if (confirmButton.getType() == confirmDisabledButton.getType()) {
            if (confirmDisabledButton.getItemMeta().hasCustomModelData()) {
                confirmChangeType = ConfirmChange.IMAGE;
            } else {
                confirmChangeType = ConfirmChange.NONE;
            }
        } else {
            confirmChangeType = ConfirmChange.TYPE;
        }

        setItem(size - 9, cancelButton, this::onCancel);
        setItem(size - 1, confirmButton, event -> {
            if (canConfirm()) {
                onConfirm(event);
                player.sendMessage(successfulConfirmMessage());
            } else {
                player.sendMessage(failedConfirmMessage());
            }
        });

        updateConfirmButtonAppearance();
    }

    protected abstract boolean canConfirm();

    protected abstract String failedConfirmMessage();

    protected abstract String successfulConfirmMessage();

    protected abstract void onConfirm(InventoryClickEvent event);

    protected abstract void onCancel(InventoryClickEvent event);

    public abstract List<String> confirmLore();
    protected void updateConfirmButtonAppearance() {

        List<String> lore = confirmLore();

        // Update the confirm button's lore
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setLore(lore);
        confirmButton.setItemMeta(confirmMeta);

        ItemMeta confirmDisabledMeta = confirmDisabledButton.getItemMeta();
        confirmDisabledMeta.setLore(lore);
        confirmDisabledButton.setItemMeta(confirmDisabledMeta);

        switch (confirmChangeType) {
            case TYPE -> {
                if (canConfirm()) {
                    inventory.setItem(inventory.getSize() - 1, confirmButton);
                } else {
                    inventory.setItem(inventory.getSize() - 1, confirmDisabledButton);
                }
            }
            case IMAGE -> {
                ItemStack updatedConfirmButton = cloneItemWithNewCustomModelData(confirmButton, canConfirm() ? confirmButton.getItemMeta().getCustomModelData() : confirmDisabledButton.getItemMeta().getCustomModelData());
                inventory.setItem(inventory.getSize() - 1, updatedConfirmButton);
            }
            case NONE -> inventory.setItem(inventory.getSize() - 1, confirmButton);
        }
    }

    private ItemStack cloneItemWithNewCustomModelData(ItemStack item, int newCustomModelData) {
        ItemStack clonedItem = item.clone();
        clonedItem.getItemMeta().setCustomModelData(newCustomModelData);
        return clonedItem;
    }

    enum ConfirmChange {
        TYPE, IMAGE, NONE;
    }
}
