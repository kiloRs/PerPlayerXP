package com.profilesplus.commands;

import com.profilesplus.RPGProfiles;
import com.profilesplus.menu.ProfileCreateMenu;
import com.profilesplus.players.PlayerData;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfileCreateCommand implements CommandExecutor {
    private final RPGProfiles plugin;

    public ProfileCreateCommand(RPGProfiles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        if (args.length != 0){
            return false;
        }

        // Check if the player has available slots for creating a new profile
        PlayerData playerData = PlayerData.get(player);
        int firstAvailableProfileSlot = playerData.findFirstAvailableProfileSlot();
        if (firstAvailableProfileSlot <= 0) {
            player.sendMessage(ChatColor.RED + "You don't have any available slots for creating a new profile.");
            return true;
        }

        // Open the profile creation menu
        new ProfileCreateMenu(playerData,null).open();

        return true;
    }
}
