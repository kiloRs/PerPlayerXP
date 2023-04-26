package com.profilesplus.menu;


import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProfileRemoveMenu extends ConfirmCancelMenu {
    private final PlayerData playerData;
    private Profile profile;


    public ProfileRemoveMenu(PlayerData playerData, Profile profile, @Nullable CharSelectionMenu selectionMenu){
        this(playerData,profile,selectionMenu,null);
    }
    public ProfileRemoveMenu(PlayerData playerData, Profile profile, @Nullable CharSelectionMenu menu, HumanEntity view) {
        super(playerData.getPlayer(), "Remove Profile", 18,"REMOVE",view);
        this.playerData = playerData;
        this.profile = profile;

        if (profile == null){
            close(CloseReason.ERROR);
            return;
        }
        if (menu != null){
            setPreviousMenu(menu);
        }

        // Display profile in the center of the top row
        setItem(4, profile.getIcon().getItemStack(), event -> {

        });
    }

    @Override
    protected boolean canConfirm() {
        return playerData.getProfiles().containsValue(profile) && !playerData.isActive(profile.getIndex());
    }

    @Override
    protected String failedConfirmMessage() {
        return getMessage("confirm.failure","You cannot remove this profile!");
    }

    @Override
    protected String successfulConfirmMessage() {
        return getMessage("confirm.successful","You have removed a profile!");
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        if (playerData.removeProfile(profile)){
            if (hasPreviousMenu()){
                close(CloseReason.CONFIRM_OPEN_NEW);
            }
            else {
                close(CloseReason.CONFIRM);
            }
        }
        else {
            close(CloseReason.ERROR);
        }
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        // Close the ClassSelectionMenu and return to the ProfileCreateMenu
        if (hasPreviousMenu(ProfileCreateMenu.class)){
            close(CloseReason.CANCEL);
            return;
        }
        close(CloseReason.NONE);
    }
    @Override
    public List<String> confirmLore() {
        String clickText = getMessage("confirm.prompt","Click here to Remove the Profile %profile_index%").replace("%profile_index%", String.valueOf(profile.getIndex()));
        List<String> lore = new ArrayList<>();
        lore.add(PlaceholderAPI.setPlaceholders(player,clickText));
        return lore;
    }

    @Override
    public void clear() {
        profile = null;
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {

        if (getCloseReason() == CloseReason.CANCEL){
            getPreviousMenu().open();
            return;
        }
        if (getCloseReason() == CloseReason.CANCEL_OPEN_NEW|| getCloseReason() == CloseReason.CONFIRM_OPEN_NEW){

            getPreviousMenu().open();
        }
        if (getCloseReason() == CloseReason.NONE){
            return;
        }
        if (getCloseReason() == CloseReason.CONFIRM){
            return;
        }
        if (getCloseReason() == CloseReason.ERROR){
            RPGProfiles.debug("Profile is not existing for removal!");
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.inventory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ProfileRemoveMenu that)) return false;

        return new EqualsBuilder().append(playerData, that.playerData).append(profile, that.profile).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(playerData).append(profile).toHashCode();
    }
}
