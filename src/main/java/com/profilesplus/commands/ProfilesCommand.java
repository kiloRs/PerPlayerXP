package com.profilesplus.commands;

import com.profilesplus.RPGProfiles;
import com.profilesplus.menu.ProfilesMenu;
import com.profilesplus.players.PlayerData;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfilesCommand implements CommandExecutor {

    private final RPGProfiles RPGProfiles;

    public ProfilesCommand(RPGProfiles RPGProfiles) {
        this.RPGProfiles = RPGProfiles;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        PlayerData playerData = PlayerData.get(player);
        if (args.length != 0){
            return false;
        }

        new ProfilesMenu(RPGProfiles.getInstance(),playerData).open();
        return true;
    }
}
