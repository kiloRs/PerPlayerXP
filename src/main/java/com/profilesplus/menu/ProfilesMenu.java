package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
        super(playerData.getPlayer(), plugin, "Profiles Menu", 54);
        this.playerData = playerData;
        Player player = playerData.getPlayer();

        // Display profiles in the center of the GUI
        for (int i = 0; i < 15; i++) {
            int slot = getCenterSlots()[i];
            Profile profile = playerData.getProfiles().get(ProfilesMenu.inventorySlotToProfileSlot(slot));

            ItemStack icon;

            boolean hasPermission = i < 5 || player.hasPermission("rpgprofiles.slot." + (i + 1));

            if (profile != null && hasPermission) {
                icon = profile.getIcon().getItemStack();
            } else {
                if (hasPermission) {
                    icon = ProfilesPlus.getIcons(player).getAvailable();
                } else {
                    icon = ProfilesPlus.getIcons(player).getLocked();
                }
            }

            setItem(slot, icon, event -> {
                slotUse = inventorySlotToProfileSlot(slot);
                if (profile != null && hasPermission) {
                    // Left click to activate profile, shift-right click to delete profile
                    if (event.isLeftClick()) {
                        profile.update();
                        playerData.getPlayer().sendMessage("Profile activated.");
                    } else if (event.isShiftClick() && event.isRightClick()) {
                        new ProfileRemoveMenu(playerData,profile,this).open();
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
    public void handleCloseEvent(InventoryCloseEvent event) {
        // Handle any additional close events for the ProfilesMenu
        if (event.getPlayer().hasMetadata("profile")){
            if (!event.getPlayer().getMetadata("profile").get(0).asBoolean()){
                ((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager().setWaiting(((Player) event.getPlayer()));
                event.getPlayer().teleport(((ProfilesPlus) ProfilesPlus.getInstance()).getSpectatorManager().getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                ((Player) event.getPlayer()).setFlySpeed(0);

                event.getPlayer().sendMessage("You must select a profile or create a new profile!");
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
        centerSlots = new int[5 * 3];
        int inventorySize = 54;
        int slotsPerRow = 9;

        // Calculate the starting slot for the center area
        int centerRowStart = (inventorySize / 2) - (slotsPerRow / 2) - 2;
        int index = 0;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                centerSlots[index] = centerRowStart + column + (row * slotsPerRow);
                index++;
            }
        }

        return centerSlots;
    }
}
