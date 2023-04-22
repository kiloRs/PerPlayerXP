package com.profilesplus.listeners;

import com.profilesplus.ItemOwnership;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerItemListener implements Listener {

        private final Map<Item, ItemOwnership> itemOwnerships = new HashMap<>();
        private final int ownershipDuration;

        public PlayerItemListener(Plugin plugin) {
            this.ownershipDuration = plugin.getConfig().getInt("item-ownership-duration") * 60 * 1000; // Convert to milliseconds
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        @EventHandler
        public void onMythicMobDeath(MythicMobDeathEvent event) {
            if (!(event.getKiller() instanceof Player)) {
                return;
            }

            Player killer = (Player) event.getKiller();
            long expiration = System.currentTimeMillis() + ownershipDuration;

            // Replace "event.getDrops()" with the method that provides the items dropped by the mob
            for (ItemStack itemStack : event.getDrops()) {
          
                event.getDrops().remove(itemStack);
                Item droppedItem = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack);
                itemOwnerships.put(droppedItem, new ItemOwnership(killer.getUniqueId(), expiration));
            }
        }

        @EventHandler
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            Item item = event.getItem();
            ItemOwnership ownership = itemOwnerships.get(item);

            if (ownership == null) {
                return;
            }

            if (System.currentTimeMillis() > ownership.getExpiration()) {
                itemOwnerships.remove(item);
                return;
            }

            if (!event.getPlayer().getUniqueId().equals(ownership.getOwner())) {
                event.setCancelled(true);
            }
        }
    }



}