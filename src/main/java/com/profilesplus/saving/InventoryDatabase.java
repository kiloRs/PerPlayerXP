package com.profilesplus.saving;

import com.profilesplus.RPGProfiles;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.UUID;

public class InventoryDatabase {
    private Connection connection;

    public InventoryDatabase(String fileName) {
        Path path = Paths.get(RPGProfiles.getInstance().getDataFolder().getPath(), fileName);
        try {
            String url = "jdbc:sqlite:" + path;
            connection = DriverManager.getConnection(url);
            createTable();
        } catch (SQLException e) {
            System.out.println("Error connecting to the database:");
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS inventories ("
                + "uuid TEXT NOT NULL,"
                + "profile_index INTEGER NOT NULL,"
                + "inventory_data TEXT,"
                + "PRIMARY KEY (uuid, profile_index));";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveInventory(String uuid, int profileIndex, String inventoryData) {
        String sql = "INSERT OR REPLACE INTO inventories(uuid, profile_index, inventory_data) VALUES(?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setInt(2, profileIndex);
            pstmt.setString(3, inventoryData);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String loadInventory(String uuid, int profileIndex) {
        String sql = "SELECT inventory_data FROM inventories WHERE uuid = ? AND profile_index = ?";
        String inventoryData = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setInt(2, profileIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                inventoryData = rs.getString("inventory_data");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return inventoryData;
    }

    public boolean hasSavedInventory(UUID uuid, int profileIndex) {
        String sql = "SELECT COUNT(*) FROM inventories WHERE uuid = ? AND profile_index = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, profileIndex);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
