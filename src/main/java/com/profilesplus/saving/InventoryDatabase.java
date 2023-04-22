package com.profilesplus.saving;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryDatabase {
    private Connection connection;

    public InventoryDatabase(String fileName) {
        try {
            String url = "jdbc:sqlite:" + fileName;
            connection = DriverManager.getConnection(url);
            createTable();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS inventories ("
                + "uuid TEXT NOT NULL,"
                + "profile_index INTEGER NOT NULL,"
                + "inventory_data BLOB,"
                + "PRIMARY KEY (uuid, profile_index));";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveInventory(String uuid, int profileIndex, byte[] inventoryData) {
        String sql = "INSERT OR REPLACE INTO inventories(uuid, profile_index, inventory_data) VALUES(?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setInt(2, profileIndex);
            pstmt.setBytes(3, inventoryData);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public byte[] loadInventory(String uuid, int profileIndex) {
        String sql = "SELECT inventory_data FROM inventories WHERE uuid = ? AND profile_index = ?";
        byte[] inventoryData = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setInt(2, profileIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                inventoryData = rs.getBytes("inventory_data");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return inventoryData;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
