package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfilesMenu extends InventoryGUI {
    private final PlayerData playerData;
    private static final int[] centerSlots =new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 26};
    private int slotUse = 0;

    public ProfilesMenu(Plugin plugin, PlayerData playerData) {
        super(playerData.getPlayer(), plugin, "Profiles Menu", 27);
        this.playerData = playerData;
        Player player = playerData.getPlayer();

        // Display profiles in the center of the GUI
        for (int i = 0; i < 15; i++) {
            int slot = centerSlots[i];
            Profile profile = playerData.getProfiles().get(ProfilesMenu.inventorySlotToProfileSlot(slot));

            ItemStack icon;

            boolean hasPermission = i < 5 || player.hasPermission("rpgprofiles.slot." + (i + 1));

            if (profile != null && hasPermission) {
                icon = profile.getIcon().getItemStack();
            } else {
                if (hasPermission) {
                    icon = ProfilesPlus.getIcons().getAvailable();
                } else {
                    icon = ProfilesPlus.getIcons().getLocked();
                }
            }

            int finalI = i;
            setItem(slot, icon, event -> {
                slotUse = inventorySlotToProfileSlot(slot);
                if (profile != null && hasPermission) {
                    // Left click to activate profile, shift-right click to delete profile
                    if (event.isLeftClick()) {
                        profile.update();
                        playerData.getPlayer().sendMessage("Profile activated.");
                    } else if (event.isShiftClick() && event.isRightClick()) {
                        new ProfileRemoveMenu(playerData,profile).open();
                    }
                } else if (profile == null && hasPermission) {
                    new ProfileCreateMenu(playerData,this).open();
                } else if (profile != null && !hasPermission) {
                    player.sendMessage("You cannot delete the profile of a locked slot.");
                }
            });
        }
    }

    @Override
    public void handleClickEvent(InventoryClickEvent event) {
        // Handle any additional click events for the ProfilesMenu
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
        // Handle any additional close events for the ProfilesMenu
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    // Helper method to convert Bukkit inventory slot to profile slot
    public static int inventorySlotToProfileSlot(int inventorySlot) {
        for (int i = 0; i < centerSlots.length; i++) {
            if (centerSlots[i] == inventorySlot) {
                return i + 1; // Add 1 to make profile slots start at 1
            }
        }
        return -1; // Return -1 if not a valid slot
    }

    // Helper method to convert profile slot to Bukkit inventory slot
    public static int profileSlotToInventorySlot(int profileSlot) {
        profileSlot -= 1; // Subtract 1 to adjust profile slots starting at 1
        if (profileSlot >= 0 && profileSlot < centerSlots.length) {
            return centerSlots[profileSlot];
        }
        return -1; // Return -1 if not a valid slot
    }

}
