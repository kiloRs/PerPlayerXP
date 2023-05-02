package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import com.profilesplus.listeners.text.NameInput;
import com.profilesplus.players.PlayerData;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ProfileCreateMenu extends ConfirmCancelMenu {
    private final int slotUse;
    private String className = null;
    private String profileName = null;
    private final int setClassTypeSlot;
    private final int setNameSlot;
    private boolean input = false;


    public ProfileCreateMenu(@NotNull PlayerData playerData, @Nullable CharSelectionMenu selectionMenu,@Nullable HumanEntity externalPlayer){
        super(playerData.getPlayer(),"Profile Creation",27,"CREATE",externalPlayer);

        if (selectionMenu != null) {
            setPreviousMenu(selectionMenu);
        }
        this.input = false;

        if (hasPreviousMenu()) {
            slotUse = ((CharSelectionMenu) getPreviousMenu()).getSlotUse();
            if (slotUse <= 0) {
                throw new RuntimeException("No Slot Use Number in Creation Menu");
            }
        }
        else {

            int firstAvailable = playerData.findFirstAvailableProfileSlot();

            if (firstAvailable <= 0){
                if (hasExternalViewer()){
                    getExternalView().sendMessage(getMessage("full","&eYou have no slots currently available!"));
                }
                else {
                    player.sendMessage(getMessage("full", "&eYou have no slots currently available!"));
                }
                slotUse = firstAvailable;
                setClassTypeSlot = -1;
                setNameSlot = -1;
                close(CloseReason.NONE);
                return;
            }
            slotUse = firstAvailable;
        }

        // Add your items to the inventory here
        // ...

        // Set the "Set Class Type" slot and add the click listener
        setClassTypeSlot = 12;
        setItem(setClassTypeSlot, RPGProfiles.getIcons(getPlayerData().getPlayer()).getClassName(), event -> {
           if (!hasExternalViewer()) {
               playerData.getPlayer().sendMessage(getMessage("prompts.class", "&ePlease select your profile's class type!"));
            ClassSelectionMenu classSelectionMenu = new ClassSelectionMenu(playerData.getPlayer(), this);
            classSelectionMenu.open();
           }
           else {
               ClassSelectionMenu classSelectionMenu = new ClassSelectionMenu(playerData.getPlayer(), this,getExternalView());
               close(CloseReason.CONFIRM);
               classSelectionMenu.open();
           }
        });

        // Set the "Set Name" slot and add the click listener
        setNameSlot = 14;
        setItem(setNameSlot, RPGProfiles.getIcons(getPlayerData().getPlayer()).getName(), event -> {
            playerData.getPlayer().setMetadata("textInput", new FixedMetadataValue(RPGProfiles.getInstance(), true));
            playerData.getPlayer().sendMessage(getMessage("prompts.name", "&ePlease select your profile's name!"));

            // Register the NameInput listener
            input = true;
            NameInput nameInput = new NameInput(this);
            Bukkit.getPluginManager().registerEvents(nameInput, RPGProfiles.getInstance());
            close();
        });
    }
    public ProfileCreateMenu(PlayerData playerData,@Nullable CharSelectionMenu menu) {
        this(playerData,menu,null);

    }

    @Override
    protected boolean canConfirm() {
        boolean changeProfiles = playerData.canChangeProfiles();
        if (!changeProfiles){
            String message = RPGProfiles.getMessage(playerData.getPlayer(), "prohibited.notify", "&aYou are prohibited from using this menu while &b(In Combat/Sleeping/Flying/Casting)");
            playerData.getPlayer().sendMessage(message);
            return false;
        }
        return className != null && profileName != null && !profileName.isEmpty() && RPGProfiles.getProfileConfigManager().canUse(player,profileName,false) && !className.isEmpty() && MMOCore.plugin.classManager.has(className) && changeProfiles;
    }

    @Override
    protected String failedConfirmMessage() {
        return getMessage("confirm.failure","&cYou cannot confirm yet!");
    }

    @Override
    protected String successfulConfirmMessage() {


        return getMessage("confirm.successful","&eYou have successfully created a profile called %profiles_name% (%profiles_class_name%)" ).replace("%profiles_class_name%",className.toUpperCase()).replace("%profiles_name%",profileName.toUpperCase());
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        if (slotUse > 0) {

            playerData.createNewProfileInSlot(profileName, className, !hasPreviousMenu() || !((CharSelectionMenu) getPreviousMenu()).isShift(),slotUse,  false);
        }
        else {
            playerData.createNewProfile(profileName,className, !hasPreviousMenu() || !((CharSelectionMenu) getPreviousMenu()).isShift());
        }
        if (hasPreviousMenu()){
            close(CloseReason.CONFIRM_OPEN_NEW);
            return;
        }
        close(CloseReason.CONFIRM);
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        if (hasPreviousMenu()){
            close(CloseReason.CANCEL_OPEN_NEW);
            getPreviousMenu().open();
            return;
        }
        close(CloseReason.CANCEL);
    }

    @Override
    public List<String> confirmLore() {
        List<String> lore = new ArrayList<>();
        if (profileName != null && !profileName.isEmpty() && !profileName.equalsIgnoreCase("cancel") && !profileName.equalsIgnoreCase("close") && !RPGProfiles.getProfileConfigManager().isForbiddenName(profileName)) {
            lore.add(ChatColor.GREEN + "Name: " + profileName);
        } else {
            lore.add(ChatColor.RED + "Name: [EMPTY]");
        }
        if (className != null && !className.isEmpty()) {
            lore.add(ChatColor.GREEN + "Class: " + className);
        } else {
            lore.add(ChatColor.RED + "Class: [EMPTY]");
        }

        return lore;
    }


    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
//        if (input) {
//            return;
//        }
//
//        if (getCloseReason() == CloseReason.CONFIRM_OPEN_NEW){
//            getPreviousMenu().open();
//            return;
//        }
//        if (getCloseReason() == CloseReason.CONFIRM){
//            return;
//        }
//        if (getCloseReason() == CloseReason.CANCEL){
//            return;
//        }
//        if (getCloseReason() == CloseReason.CANCEL_OPEN_NEW){
//            getPreviousMenu().open();
//            return;
//        }
//        if (getCloseReason() == CloseReason.NONE){
//            return;
//        }
//        if (getCloseReason() == CloseReason.ERROR){
//            throw new RuntimeException("ERROR: ProfileCreateMenu");
//        }
    }
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setProfileName(String name) {
        this.profileName = name;

        updateConfirmButtonAppearance();
    }


    public void setClassType(String classType) {
        this.className = classType;

        updateConfirmButtonAppearance();
    }

    @Override
    public void clear() {
        this.className = null;
        this.profileName = null;
        this.input = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ProfileCreateMenu that)) return false;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(getSlotUse(), that.getSlotUse()).append(getSetClassTypeSlot(), that.getSetClassTypeSlot()).append(getSetNameSlot(), that.getSetNameSlot()).append(isInput(), that.isInput()).append(getClassName(), that.getClassName()).append(getProfileName(), that.getProfileName()).append(getPlayerData(), that.getPlayerData()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getSlotUse()).append(getClassName()).append(getProfileName()).append(getSetClassTypeSlot()).append(getSetNameSlot()).append(getPlayerData()).append(isInput()).toHashCode();
    }
}
