package com.profilesplus.commans;


import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProfilesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length > 0){
            return false;
        }
        PlayerData playerData = PlayerData.get(player.getUniqueId());


        ProfilesMenu profilesMenu = ProfilesMenu.use(playerData);

        // Open the ProfilesMenu for the player
        // ...

        try {
            profilesMenu.open();
            return true;
        } catch (Exception e) {
            return false;
    }
    }
}
