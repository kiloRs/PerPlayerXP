package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public abstract class ConfirmCancelMenu extends InventoryGUI {
    private final Consumer<InventoryClickEvent> onConfirm;
    private final Consumer<InventoryClickEvent> onCancel;

    public ConfirmCancelMenu(Player player, Plugin plugin, String title, int size, Consumer<InventoryClickEvent> onConfirm, Consumer<InventoryClickEvent> onCancel) {
        super(player, plugin, title, size);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        setItem(26, getConfirmButton(), onConfirm);
        setItem(18, getCancelButton(), onCancel);
    }

    public ItemStack getConfirmButton() {
        return ProfilesPlus.getIcons().getConfirm();
    }

    public ItemStack getCancelButton() {
        return ProfilesPlus.getIcons().getCancel();
    }
}

