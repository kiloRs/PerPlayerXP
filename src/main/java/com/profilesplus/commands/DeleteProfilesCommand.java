package com.profilesplus.commands;

import com.profilesplus.menu.ProfilesDeleteMenu;
import com.profilesplus.players.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteProfileCommand implements CommandExecutor {
    private final PlayerData playerData;

    public DeleteProfileCommand(PlayerData playerData) {
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        new ProfilesDeleteMenu(playerData).open(player);
        return true;
    }
}
