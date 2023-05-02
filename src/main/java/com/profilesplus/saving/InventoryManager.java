package com.profilesplus.saving;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class InventoryManager {
    private final InventoryDatabase database;

    public InventoryManager(String fileName) {
        this.database = new InventoryDatabase(fileName);
    }

    public void saveInventory(Player player, int profileIndex) {
        UUID uuid = player.getUniqueId();
        try {
            String serializedInventory = InventorySerialization.serializeInventory(player.getInventory());
            database.saveInventory(uuid.toString(), profileIndex, serializedInventory);
            System.out.println("Inventory saved for player: " + player.getName() + " Profile: " + profileIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadInventory(Player player, int profileIndex) {
        UUID uuid = player.getUniqueId();
        String serializedInventory = database.loadInventory(uuid.toString(), profileIndex);
        if (serializedInventory != null) {
            try {
                PlayerInventory inventory = InventorySerialization.deserializeInventory(player, serializedInventory);
                player.getInventory().setContents(inventory.getContents());
                player.getInventory().setArmorContents(inventory.getArmorContents());
                System.out.println("Inventory loaded for player: " + player.getName() + " Profile: " + profileIndex);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No saved inventory found for player: " + player.getName() + " Profile: " + profileIndex);
        }
    }

    public boolean hasSavedInventory(Player player, int profileIndex) {
        return database.hasSavedInventory(player.getUniqueId(), profileIndex);
    }
    public int getItemsInInventory(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack i : getInventory(player)) {
            if (item == null && i != null && i.getType()!=Material.AIR){
                count+= i.getAmount();
            }
            if (i != null && i.getType()!=(Material.AIR) && i.getType() == item.getType() && i.getAmount() >= item.getAmount()) {
                count += i.getAmount();
            }
        }
        return count;
    }

    public ItemStack[] getInventory(Player player) {
        String inventoryData = database.loadInventory(player.getUniqueId().toString(), 0);
        if (inventoryData != null) {
            try {
                return InventorySerialization.deserializeInventory(player, inventoryData).getContents();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ItemStack[0];
    }

    public List<ItemStack> getInventoryAsList(Player player) {
        return Arrays.asList(getInventory(player));
    }

    public void close() {
        database.close();
    }
}
