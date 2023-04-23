package com.profilesplus.menu;


import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProfileRemoveMenu extends ConfirmCancelMenu {
    private final PlayerData playerData;
    private final Profile profile;
    private final ProfilesMenu profilesMenu;

    public ProfileRemoveMenu(PlayerData playerData, Profile profile,@Nullable ProfilesMenu menu) {
        super(playerData.getPlayer(), RPGProfiles.getInstance(), "Remove Profile", 18);
        this.playerData = playerData;
        this.profile = profile;
        this.profilesMenu = menu;

        // Display profile in the center of the top row
        setItem(4, profile.getIcon().getItemStack(), event -> {});
    }

    @Override
    protected boolean canConfirm() {
        return true;
    }

    @Override
    protected String failedConfirmMessage() {
        return "Error with removing";
    }

    @Override
    protected String successfulConfirmMessage() {
        return "You have successfully removed the profile in " + profile.getIndex() + " named " + profile.getId();
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        // Remove the profile from the player's profiles map and config
        if (playerData.isActive(profile.getIndex()) && playerData.getProfiles().size() == 1){
            close(InventoryCloseEvent.Reason.CANT_USE);
            player.sendMessage("You cannot remove your final profile!");
            event.setCancelled(true);
        }
        playerData.removeProfile(profile);

        // Reopen the ProfilesMenu to update the GUI
        close(InventoryCloseEvent.Reason.PLUGIN);
        new ProfilesMenu(RPGProfiles.getInstance(), playerData).open();

    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        // Close the menu and do nothing
        if (profilesMenu == null){
            close(InventoryCloseEvent.Reason.CANT_USE);
            return;
        }
        else {
            close(InventoryCloseEvent.Reason.PLUGIN);
            profilesMenu.open();
        }
    }

    @Override
    public List<String> confirmLore() {
        String clickText = "Click here to confirm deletion of the " + profile.getId() + " profile.";
        List<String> lore = new ArrayList<>();
        lore.add(PlaceholderAPI.setPlaceholders(player,clickText));
        return lore;
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
        if (profilesMenu== null){
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        }
        else {
            profilesMenu.open();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.inventory;
    }
}
