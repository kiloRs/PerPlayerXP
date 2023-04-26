package com.profilesplus.saving;


import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
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

    public void close() {
        database.close();
    }
}
