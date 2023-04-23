package com.profilesplus.listeners;

import com.profilesplus.RPGProfiles;
import com.profilesplus.SpectatorManager;
import com.profilesplus.menu.ClassSelectionMenu;
import com.profilesplus.menu.InventoryGUI;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.menu.ProfilesMenu;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (event.getClickedInventory().getHolder() instanceof InventoryGUI inventoryGUI){
            if (RPGProfiles.isLogging()){
                RPGProfiles.log("Clicking InventoryGUI Menu... " + inventoryGUI.getClass().getName() + " [ RawSlot:" + event.getRawSlot() + "- Slot:" + event.getSlot() +" ]");
            }
            inventoryGUI.processClickEvent(event);
            return;
        }
        if (holder instanceof InventoryGUI inventoryGUI) {
            if (RPGProfiles.isLogging()){
                RPGProfiles.log("Clicking InventoryGUI Menu: " + inventoryGUI.getClass().getName() + " [ RawSlot:" + event.getRawSlot() + "- Slot:" + event.getSlot() +" ]");
            }
            inventoryGUI.processClickEvent(event);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)){
            return;
        }
        InventoryHolder holder = event.getInventory().getHolder();
            if (holder instanceof InventoryGUI inventoryGUI) {
                inventoryGUI.handleCloseEvent(event);
                if (RPGProfiles.isLogging()) {
                    RPGProfiles.log("Closing InventoryGUI Menu: " + inventoryGUI.getClass().getName());
                }

                if (holder instanceof ClassSelectionMenu classSelectionMenu){
                    if (classSelectionMenu.getCreateMenu() != null) {
                        if (classSelectionMenu.getSelectedClassType() != null && !classSelectionMenu.getSelectedClassType().isEmpty() && MMOCore.plugin.classManager.has(classSelectionMenu.getSelectedClassType())){
                            classSelectionMenu.getCreateMenu().setClassType(classSelectionMenu.getSelectedClassType());
                        }
                        classSelectionMenu.getCreateMenu().open();
                    }
                }

                if (holder instanceof ProfilesMenu profilesMenu || holder instanceof ProfileCreateMenu createMenu){
                    SpectatorManager manager = ((RPGProfiles) RPGProfiles.getInstance()).getSpectatorManager();
                    if (manager.isWaiting(player)) {
                        manager.warn(player);
                        manager.setWaiting(player);
                        return;
                }
                    RPGProfiles.log("Closing Menu Complete " + inventoryGUI.getClass().getSimpleName());
                    return;
                }
            }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryGUI inventoryGUI) {
            event.setCancelled(true);
            if (RPGProfiles.isLogging()){
                RPGProfiles.log("InventoryGUI Action: " + inventoryGUI.getClass().getName());
            }
        }
    }

    @EventHandler
    public void on(InventoryOpenEvent event){
        if (event.getInventory().getHolder() instanceof InventoryGUI inventoryGUI){
            if (RPGProfiles.isLogging()){
                RPGProfiles.log("Opening InventoryGUI Menu: " + inventoryGUI.getClass().getName());
            }
        }
    }
}
