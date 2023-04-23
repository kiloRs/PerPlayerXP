package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import com.profilesplus.listeners.text.NameInput;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public class ProfileCreateMenu extends ConfirmCancelMenu {
    private final ProfilesMenu profilesMenu;
    private final int slotUse;
    private String className;
    private String profileName;
    private final int setClassTypeSlot;
    private final int setNameSlot;
    private final PlayerData playerData;
    private boolean fromProfiles = false;
    private final List<String> forbiddenNames = ((RPGProfiles) RPGProfiles.getInstance()).getForbiddenNames();
    private final Pattern forbiddenNamePattern;


    private Pattern createForbiddenNamePattern() {
        StringBuilder patternBuilder = new StringBuilder("(?i).*(");
        for (int i = 0; i < forbiddenNames.size(); i++) {
            patternBuilder.append(forbiddenNames.get(i));
            if (i < forbiddenNames.size() - 1) {
                patternBuilder.append("|");
            }
        }
        patternBuilder.append(").*");
        return Pattern.compile(patternBuilder.toString());
    }

    public boolean isForbiddenName(String name) {
        return forbiddenNamePattern.matcher(name).matches();
    }

    public ProfileCreateMenu(PlayerData playerData,@Nullable ProfilesMenu menu) {
        super(playerData.getPlayer(), RPGProfiles.getInstance(), "Profile Creation", 27);
        this.playerData = playerData;
        this.profilesMenu = menu;

        if (profilesMenu != null) {
            slotUse = profilesMenu.getSlotUse();
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
        setItem(setClassTypeSlot, RPGProfiles.getIcons(getPlayerData().getPlayer()).getClassName(), event -> {
            ClassSelectionMenu classSelectionMenu = new ClassSelectionMenu(playerData.getPlayer(), this);
            classSelectionMenu.open();
        });

        // Set the "Set Name" slot and add the click listener
        setNameSlot = 14;
        setItem(setNameSlot, RPGProfiles.getIcons(getPlayerData().getPlayer()).getName(), event -> {
            playerData.getPlayer().closeInventory();
            playerData.getPlayer().sendMessage("Please type the profile name in chat.");

            playerData.getPlayer().setMetadata("textInput",new FixedMetadataValue(RPGProfiles.getInstance(),true));
            // Register the NameInput listener
            NameInput nameInput = new NameInput( this);
            Bukkit.getPluginManager().registerEvents(nameInput, plugin);
        });
        forbiddenNamePattern = createForbiddenNamePattern();
    }

    @Override
    protected boolean canConfirm() {
        return className != null && profileName != null && !profileName.isEmpty() && !className.isEmpty() && MMOCore.plugin.classManager.has(className);
    }

    @Override
    protected String failedConfirmMessage() {
        boolean profile = true;
        boolean type = true;
        if (profileName == null || profileName.isEmpty() ){
            profile = false;
        }
        if (className == null || className.isEmpty() || !MMOCore.plugin.classManager.has(className)){
            type = false;
        }

        if (profile && type){
            return "ERROR during CREATION";
        }
        else if (profile){
            return "Profile name is valid, however, the Class type is invalid or missing!";
        }
        else if (type){
            return "Selected Class type is valid, however, the Profile name is invalid or missing!";
        }
        else {
            return "You must provide a Profile name and Select a valid Class type to confirm!";
        }
    }

    @Override
    protected String successfulConfirmMessage() {
        return "You have successfully created a " + className.toUpperCase() + " profile named " + profileName.toUpperCase();
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        Profile newProfile;
        if (slotUse > 0) {
            newProfile = playerData.createNewProfileInSlot(profileName, className, !event.isShiftClick(),slotUse);
        }
        else {
            newProfile = playerData.createNewProfile(profileName,className, !event.isShiftClick());
        }
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
    public List<String> confirmLore() {
        List<String> lore = new ArrayList<>();
        if (profileName != null && !profileName.isEmpty()) {
            lore.add(ChatColor.GREEN + "Name: " + profileName);
        } else {
            lore.add(ChatColor.RED + "Name: Not Set");
        }
        if (className != null && !className.isEmpty()) {
            lore.add(ChatColor.GREEN + "Class: " + className);
        } else {
            lore.add(ChatColor.RED + "Class: Not Set");
        }

        return lore;    }


    @Override
    public void handleCloseEvent(InventoryCloseEvent event) {
        if (profilesMenu != null){
            event.getPlayer().closeInventory();
            profilesMenu.open();
        }
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


}
