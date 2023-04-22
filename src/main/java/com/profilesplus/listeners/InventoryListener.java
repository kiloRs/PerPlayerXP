package com.profilesplus.listeners;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.menu.InventoryGUI;
import org.bukkit.event.EventHandler;
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
        if (holder instanceof InventoryGUI inventoryGUI) {
            event.setCancelled(true);
            if (ProfilesPlus.isLogging()){
                ProfilesPlus.log("Clicking InventoryGUI Menu: " + inventoryGUI.getClass().getName() + " [ RawSlot:" + event.getRawSlot() + "- Slot:" + event.getSlot() +" ]");
            }
            inventoryGUI.handleClickEvent(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryGUI inventoryGUI) {
            inventoryGUI.handleCloseEvent(event);
            if (ProfilesPlus.isLogging()){
                ProfilesPlus.log("Closing InventoryGUI Menu: " + inventoryGUI.getClass().getName());
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryGUI inventoryGUI) {
            event.setCancelled(true);
            if (ProfilesPlus.isLogging()){
                ProfilesPlus.log("InventoryGUI Action: " + inventoryGUI.getClass().getName());
            }
        }
    }

    @EventHandler
    public void on(InventoryOpenEvent event){
        if (event.getInventory().getHolder() instanceof InventoryGUI inventoryGUI){
            if (ProfilesPlus.isLogging()){
                ProfilesPlus.log("Opening InventoryGUI Menu: " + inventoryGUI.getClass().getName());
            }
        }
    }
}
