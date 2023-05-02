package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@Getter
public abstract class ConfirmCancelMenu extends InventoryGUI {
    protected final ItemStack cancelButton;
    protected final ItemStack confirmButton;
    protected final ItemStack confirmDisabledButton;
    private final ConfirmChange confirmChangeType;
    private boolean closeByClick = false;
    protected CloseReason closeReason;

    public ConfirmCancelMenu(Player player, String title, int size, String key) {
        this(player,title,size,key,null);
    }

    public ConfirmCancelMenu(Player player, String profileCreation, int size, String create,@Nullable HumanEntity externalPlayer) {
        super(player, profileCreation, size, create, externalPlayer);

        resetClick();
        cancelButton = RPGProfiles.getIcons(player).getCancel();
        confirmButton = RPGProfiles.getIcons(player).getConfirm();
        confirmDisabledButton = RPGProfiles.getIcons(player).getConfirmDisabled();
        closeReason = CloseReason.NONE;
        if (confirmButton.getType() == confirmDisabledButton.getType()) {
            if (confirmDisabledButton.getItemMeta().hasCustomModelData()) {
                confirmChangeType = ConfirmChange.IMAGE;
            } else {
                confirmChangeType = ConfirmChange.NONE;
            }
        } else {
            confirmChangeType = ConfirmChange.TYPE;
        }

        setItem(size - 9, cancelButton,inventoryClickEvent -> {
            closeByClick = true;
            onCancel(inventoryClickEvent);

        });
        setItem(size - 1, confirmButton, event -> {

            if (canConfirm()) {
                closeByClick = true;
                onConfirm(event);
                if (hasExternalViewer()){
                    getExternalView().sendMessage(successfulConfirmMessage());
                    return;
                }
                else {
                player.sendMessage(successfulConfirmMessage());
                return;
            }} else {
                if (hasExternalViewer()){
                    getExternalView().sendMessage(failedConfirmMessage());
                }
                else {
                    player.sendMessage(failedConfirmMessage());
                }
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

    public abstract void clear();

    public void close(CloseReason closeReason){
        if (this.inventory.getViewers().contains(player)) {
            this.closeReason = closeReason;
            super.close();
        }
    }
    private void resetClick(){
        closeByClick = false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ConfirmCancelMenu that)) return false;

        return new EqualsBuilder().appendSuper(super.equals(o)).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).toHashCode();
    }

    enum CloseReason{
        CANCEL_OPEN_NEW,CANCEL, CONFIRM_OPEN_NEW, CONFIRM, ERROR, NONE;
    }
    enum ConfirmChange {
        TYPE, IMAGE, NONE;
    }
}
