package com.profilesplus.listeners;

import com.profilesplus.menu.ConfirmCancelMenu;
import com.profilesplus.menu.InventoryGUI;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null){
            if (event.getInventory().getHolder() instanceof InventoryGUI inventoryGUI){
                inventoryGUI.close();
                return;
            }
            return;
        }
        InventoryHolder holder = event.getInventory().getHolder();
        if (event.getClickedInventory().getHolder() instanceof InventoryGUI inventoryGUI){
            inventoryGUI.processClickEvent(event);
            return;
        }
        else if (holder instanceof InventoryGUI inventoryGUI){
            inventoryGUI.processClickEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)){
            return;
        }
        InventoryHolder holder = event.getInventory().getHolder();
            if (holder instanceof InventoryGUI inventoryGUI) {
                if (!inventoryGUI.getPlayer().getUniqueId().equals(player.getUniqueId())){
                    return;
                }
                if (inventoryGUI instanceof ConfirmCancelMenu confirmCancelMenu){
                    if (!confirmCancelMenu.isCloseByClick()){
                        return;
                    }
                }
                inventoryGUI.handleCloseEvent(event);

            }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryGUI inventoryGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(InventoryOpenEvent event){
        if (event.getInventory().getHolder() instanceof InventoryGUI inventoryGUI){
            return;
        }
    }
}
