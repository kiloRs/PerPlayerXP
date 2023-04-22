package com.profilesplus;


import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        Profile activeProfile = playerData.getActiveProfile();

        if (activeProfile == null) {
            // Open the CreateProfileMenu
            ProfileCreateMenu menu = new ProfileCreateMenu(playerData);
            menu.open();

            // Set the player to spectator mode and disable movement
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            event.getPlayer().setFlySpeed(0);
            // Use ProtocolLib or other methods to restrict movement
        } else {
            // Open the ProfilesMenu
            ProfilesMenu menu = new ProfilesMenu(playerData);
            menu.open();

            // Set the player's location to the last saved location of the active profile
            Location lastLocation = activeProfile.getLastKnownLocation();
            if (lastLocation != null) {
                event.getPlayer().teleport(lastLocation);
            }

            // Set the player's balance to the last saved balance of the active profile
            Economy economy = ((ProfilesPlus) plugin).getEconomy();
            double currentBalance = economy.getBalance(event.getPlayer());
            economy.withdrawPlayer(event.getPlayer(), currentBalance);
            economy.depositPlayer(event.getPlayer(), activeProfile.getBalance());
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Replace 'Create Profile' with the title of the inventory you want to trigger this behavior
        if (event.getInventory().getHolder() instanceof ProfileCreateMenu createMenu) {
            Location spectatorLocation = getSpectatorLocation();
            player.teleport(spectatorLocation);
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlySpeed(0);
        }
    }

    private Location getSpectatorLocation() {
        String worldName = plugin.getConfig().getString("spectator-location.world");
        if (worldName == null){
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        World world = Bukkit.getWorld(worldName);
        double x = plugin.getConfig().getDouble("spectator-location.x");
        double y = plugin.getConfig().getDouble("spectator-location.y");
        double z = plugin.getConfig().getDouble("spectator-location.z");
        float pitch = (float) plugin.getConfig().get("spectator-location.pitch",0);
        float yaw = ((float) plugin.getConfig().get("spectator-location.yaw",0));

        return new Location(world, x, y, z,yaw,pitch);
    }
}
