package com.profilesplus.menu;


import com.profilesplus.ProfilesPlus;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ProfileRemoveMenu extends ConfirmCancelMenu {
    private final PlayerData playerData;
    private final Profile profile;

    public ProfileRemoveMenu(PlayerData playerData, Profile profile) {
        super(playerData.getPlayer(), ProfilesPlus.getInstance(), "Remove Profile", 18);
        this.playerData = playerData;
        this.profile = profile;

        // Display profile in the center of the top row
        setItem(4, profile.getIcon().getItemStack(), event -> {});

        open();
    }

    @Override
    protected boolean canConfirm() {
        return true;
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        // Remove the profile from the player's profiles map and config
        playerData.removeProfile(profile);

        // Reopen the ProfilesMenu to update the GUI
        playerData.getPlayer().closeInventory();
        new ProfilesMenu(ProfilesPlus.getInstance(), playerData);

        // Send a message to the player
        playerData.getPlayer().sendMessage("Profile removed.");
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        // Close the menu and do nothing
        playerData.getPlayer().closeInventory();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent event) {

    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.inventory;
    }
}
