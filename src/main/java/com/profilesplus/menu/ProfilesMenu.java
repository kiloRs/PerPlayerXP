package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import io.lumine.mythic.lib.MythicLib;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfilesMenu extends InventoryGUI {
    private final PlayerData playerData;
    private static int[] centerSlots;
    private int slotUse = 0;

    public ProfilesMenu(Plugin plugin, PlayerData playerData) {
        super(playerData.getPlayer(), plugin, MythicLib.plugin.parseColors("Profiles"), 54);
        this.playerData = playerData;
        Player player = playerData.getPlayer();

        // Display profiles in the center of the GUI
        for (int i = 0; i < 15; i++) {
            int slot = getCenterSlots()[i];
            Profile profile = playerData.getProfiles().get(ProfilesMenu.inventorySlotToProfileSlot(slot));

            ItemStack icon;

            boolean hasPermission = i < 5 || player.hasPermission(PlayerData.getPERMISSION_PREFIX() + inventorySlotToProfileSlot(slot));

            if (profile != null && hasPermission) {
                icon = profile.getIcon().getItemStack();
            } else {
                if (hasPermission) {
                    icon = RPGProfiles.getIcons(player).getAvailable();
                } else {
                    icon = RPGProfiles.getIcons(player).getLocked();
                }
            }

            setItem(slot, icon, event -> {
                slotUse = inventorySlotToProfileSlot(slot);
                if (profile != null && hasPermission) {
                    // Left click to activate profile, shift-right click to delete profile
                    if (event.isLeftClick()) {
                        playerData.changeProfile(profile);
                        playerData.getPlayer().sendMessage("Profile activated: " + profile.getIndex() +  " " + profile.getId() + " for " + player.getName());
                    } else if (event.isShiftClick() && event.isRightClick()) {
                        this.close(InventoryCloseEvent.Reason.PLUGIN);
                        new ProfileRemoveMenu(playerData,profile,this).open();
                    }
                } else if (profile == null && hasPermission) {
                    this.close(InventoryCloseEvent.Reason.PLUGIN);
                    new ProfileCreateMenu(playerData,this).open();
                } else if (profile != null) {
                    player.sendMessage("You cannot delete the profile of a locked slot.");
                }
            });
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof InventoryGUI inventoryGUI)){
            return;
        }
        RPGProfiles.log("Menu Closing: " + inventoryGUI.getClass().getSimpleName());
        RPGProfiles.log("Reason for closing profiles menu: " + event.getReason().name());
        // Handle any additional close events for the ProfilesMenu

        if (PlayerData.get((Player) event.getPlayer()).getProfileMap().isEmpty() || PlayerData.get(((Player) event.getPlayer())).getActiveProfile() == null) {

            if (((RPGProfiles) RPGProfiles.getInstance()).getSpectatorManager().isWaiting(((Player) event.getPlayer()))) {
                ((RPGProfiles) RPGProfiles.getInstance()).getSpectatorManager().setWaiting(player);
                return;
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    // Helper method to convert Bukkit inventory slot to profile slot
    public static int inventorySlotToProfileSlot(int inventorySlot) {
        for (int i = 0; i < getCenterSlots().length; i++) {
            if (getCenterSlots()[i] == inventorySlot) {
                return i + 1; // Add 1 to make profile slots start at 1
            }
        }
        return -1; // Return -1 if not a valid slot
    }

    // Helper method to convert profile slot to Bukkit inventory slot
    public static int profileSlotToInventorySlot(int profileSlot) {
        profileSlot -= 1; // Subtract 1 to adjust profile slots starting at 1
        if (profileSlot >= 0 && profileSlot < getCenterSlots().length) {
            return getCenterSlots()[profileSlot];
        }
        return -1; // Return -1 if not a valid slot
    }
    public static int[] getCenterSlots() {
        // Directly initialize the centerSlots array with the desired slot numbers
        centerSlots = new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

        return centerSlots;
    }
}
