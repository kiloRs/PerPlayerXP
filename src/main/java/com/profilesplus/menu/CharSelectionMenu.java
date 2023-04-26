package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import io.lumine.mythic.lib.MythicLib;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CharSelectionMenu extends InventoryGUI {
    private final PlayerData playerData;
    private static int[] centerSlots;
    private int slotUse = 0;
    public CharSelectionMenu(PlayerData owning){
        this(owning,null);

    }
    public CharSelectionMenu( PlayerData playerData, HumanEntity external) {
        super(playerData.getPlayer(),  MythicLib.plugin.parseColors("Profiles"), 54,"CHAR_SELECTION",external);
        this.playerData = playerData;
        Player player = playerData.getPlayer();

        // Display profiles in the center of the GUI
        for (int i = 0; i < 15; i++) {
            int slot = getCenterSlots()[i];
            int toProfileSlot = CharSelectionMenu.inventorySlotToProfileSlot(slot);

            ItemStack icon;

            boolean hasPermission = toProfileSlot < 6 && toProfileSlot > 0 || player.hasPermission(PlayerData.getPERMISSION_PREFIX() + toProfileSlot);

            Profile profile = playerData.getProfiles().get(toProfileSlot);

            icon = updateSlot(player, profile, hasPermission);

            setItem(slot, icon, event -> {
                slotUse =  toProfileSlot;
                if (!hasPermission) {
                    getMessage("permission.failure", "&cYou are missing the permission to use this slot!");
                    return;
                }
                if (profile != null){
                    if (event.isLeftClick()){
                        if (playerData.changeProfile(profile,null)) {
                            RPGProfiles.log("Profile Change!");
                            return;
                        }
                        RPGProfiles.log("No Profile Change!");
                        return;
                    }
                    else if (event.isRightClick()){
                        new ProfileRemoveMenu(playerData,profile,this,getExternalView()).open();
                        RPGProfiles.log("Opening Removal GUI");
                    }
                }
                else {
                    ProfileCreateMenu menu = new ProfileCreateMenu(playerData, this,getExternalView());
                    menu.open();
                    RPGProfiles.log("Opening Creation GUI");
                }
            });
        }
    }

    private static ItemStack updateSlot(Player player, Profile profile, boolean hasPermission) {
        ItemStack icon;
        if (profile != null && hasPermission) {
            icon = profile.getIcon().getItemStack();
            ItemMeta iconMeta = icon.getItemMeta();

            List<String> lore = iconMeta.hasLore() ? iconMeta.getLore() : new ArrayList<>();
            lore.add(0, ChatColor.GRAY + RPGProfiles.getMessage(player,"profile.clicks.prompt","&eLeft Click to Select &7- &eShift-Right Click to Remove"));

            iconMeta.setLore(lore);
            icon.setItemMeta(iconMeta);
        } else {
            if (hasPermission) {
                icon = RPGProfiles.getIcons(player).getAvailable();
            } else {
                icon = RPGProfiles.getIcons(player).getLocked();
            }
        }
        return icon;
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
        if (hasExternalViewer()){
            return;
        }
        if (slotUse > 0 && slotUse < 16) {
            player.playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_ANVIL_USE.key()).source(Sound.Source.BLOCK).volume(0.33f).pitch(3f).build());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof CharSelectionMenu that)) return false;

        return new EqualsBuilder().append(getSlotUse(), that.getSlotUse()).append(getPlayerData(), that.getPlayerData()).appendSuper(true).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPlayerData()).append(getSlotUse()).toHashCode();
    }
}
