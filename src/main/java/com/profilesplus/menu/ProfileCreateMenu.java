package com.profilesplus.menu;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.menu.text.NameInput;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import net.Indyuce.mmocore.MMOCore;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProfileCreateMenu extends ConfirmCancelMenu {
    private final ProfilesMenu profilesMenu;
    private final int slotUse;
    private String className;
    private String profileName;
    private final int setClassTypeSlot;
    private final int setNameSlot;
    private final PlayerData playerData;
    private boolean fromProfiles = false;

    public ProfileCreateMenu(PlayerData playerData,@Nullable ProfilesMenu menu) {
        super(playerData.getPlayer(), ProfilesPlus.getInstance(), "Profile Creation", 27);
        this.playerData = playerData;
        this.profilesMenu = menu;

        if (profilesMenu != null) {
            slotUse = menu.getSlotUse();
            if (slotUse <= 0) {
                throw new RuntimeException("No Slot Use Number in Creation Menu");
            }
            fromProfiles = true;
        }
        else {

            int firstAvailable = playerData.findFirstAvailableProfileSlot();

            if (firstAvailable <= 0){
                throw new RuntimeException("No Slot Use Number to Create Profile!");
            }
            slotUse = firstAvailable;
        }

        // Add your items to the inventory here
        // ...

        // Set the "Set Class Type" slot and add the click listener
        setClassTypeSlot = 12;
        setItem(setClassTypeSlot, ProfilesPlus.getIcons().getClassName(), event -> {
            ClassSelectionMenu classSelectionMenu = new ClassSelectionMenu(playerData.getPlayer(), this);
            classSelectionMenu.open();
        });

        // Set the "Set Name" slot and add the click listener
        setNameSlot = 14;
        setItem(setNameSlot, ProfilesPlus.getIcons().getName(), event -> {
            playerData.getPlayer().closeInventory();
            playerData.getPlayer().sendMessage("Please type the profile name in chat.");

            // Register the NameInput listener
            NameInput nameInput = new NameInput(playerData.getPlayer(), playerData, this);
            Bukkit.getPluginManager().registerEvents(nameInput, plugin);
        });
    }

    @Override
    protected boolean canConfirm() {
        return className != null && profileName != null && !profileName.isEmpty() && !className.isEmpty() && MMOCore.plugin.classManager.has(className);
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        Profile newProfile = playerData.createNewProfile(profileName, className, true);
        if (newProfile != null) {
            playerData.getPlayer().sendMessage(ChatColor.YELLOW + "Profile Creation Complete! ");
        }
        else {
            playerData.getPlayer().sendMessage(ChatColor.RED + "Error in creating Profile for " + event.getWhoClicked().getName());
        }
        className = null;
        profileName = null;
        playerData.getPlayer().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        if (!fromProfiles){
            playerData.getPlayer().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            return;
        }
        ProfilesMenu profilesMenu = new ProfilesMenu(plugin, playerData);
        profilesMenu.open();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent event) {

    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setProfileName(String name) {
        this.profileName = name;
        updateConfirmButton();
    }


    public void setClassType(String classType) {
        this.className = classType;
    }


}
