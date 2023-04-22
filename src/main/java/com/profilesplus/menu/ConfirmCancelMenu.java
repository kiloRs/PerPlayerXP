package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class ConfirmCancelMenu extends InventoryGUI {
    protected final ItemStack cancelButton;
    protected final ItemStack confirmButton;

    public ConfirmCancelMenu(Player player, Plugin plugin, String title, int size) {
        super(player, plugin, title, size);

        cancelButton = ProfilesPlus.getIcons().getCancel();
        confirmButton = ProfilesPlus.getIcons().getConfirm();

        setItem(size - 9, cancelButton, this::onCancel);
        setItem(size - 1, confirmButton, event -> {
            if (canConfirm()) {
                onConfirm(event);
            }
        });
    }

    protected abstract boolean canConfirm();

    protected abstract void onConfirm(InventoryClickEvent event);

    protected abstract void onCancel(InventoryClickEvent event);

    protected void updateConfirmButton() {
        inventory.setItem(inventory.getSize() - 1, confirmButton);
    }
}
