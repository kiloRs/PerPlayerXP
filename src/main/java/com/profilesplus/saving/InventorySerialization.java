package com.profilesplus.saving;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventorySerialization {

    public static String serializeInventory(PlayerInventory inventory) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Save armor contents
            dataOutput.writeInt(inventory.getArmorContents().length);
            for (ItemStack armorItem : inventory.getArmorContents()) {
                dataOutput.writeObject(armorItem);
            }

            // Save main inventory
            dataOutput.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
    }

    public static PlayerInventory deserializeInventory(Player player, String data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            PlayerInventory inventory = player.getInventory();

            // Load armor contents
            int armorSize = dataInput.readInt();
            ItemStack[] armorContents = new ItemStack[armorSize];
            for (int i = 0; i < armorSize; i++) {
                armorContents[i] = (ItemStack) dataInput.readObject();
            }
            inventory.setArmorContents(armorContents);

            // Load main inventory
            int mainSize = dataInput.readInt();
            for (int i = 0; i < mainSize; i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        }
    }
}
