package com.profilesplus.menu;

import com.profilesplus.RPGProfiles;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class InventoryGUI implements InventoryHolder {
    protected final Player player;
    protected final Plugin plugin;
    protected final String title;
    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> actions;

    public InventoryGUI(Player player, Plugin plugin, String title, int size) {
        this.player = player;
        this.plugin = plugin;
        this.title = title;
        this.inventory = plugin.getServer().createInventory(this, size, title);
        this.actions = new HashMap<>();
    }

    public void open() {
        new BukkitRunnable(){
            @Override
            public void run() {
                player.openInventory(InventoryGUI.this.inventory);
            }
        }.runTask(RPGProfiles.getInstance());
    }


    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        inventory.setItem(slot, item);
        actions.put(slot, onClick.andThen(inventoryClickEvent -> inventoryClickEvent.setCancelled(true)));
    }

    public void processClickEvent(InventoryClickEvent event) {
        // Process the click event based on the actions map
//        event.setCancelled(true);
        int clickedSlot = event.getRawSlot();
        if (actions.containsKey(clickedSlot)) {
            actions.get(clickedSlot).accept(event);
        }
    }

    public void close(InventoryCloseEvent.Reason reason){
        if (inventory.getViewers().contains(player)) {
            player.closeInventory(reason);
        }
    }
    public void close(){
        close(InventoryCloseEvent.Reason.PLUGIN);
    }
    public abstract void handleCloseEvent(InventoryCloseEvent event);
}
