package com.profilesplus.commands;

import com.profilesplus.RPGProfiles;
import com.profilesplus.menu.CharSelectionMenu;
import com.profilesplus.players.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class ProfilesCommand implements CommandExecutor {

    private final RPGProfiles rpgProfiles;

    public ProfilesCommand(RPGProfiles profiles) {
        this.rpgProfiles = profiles;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender.isOp() && sender instanceof Player player){
            if (args.length > 0){
                Player playerExact = Bukkit.getPlayerExact(args[0]);
                if (playerExact != null){
                    CharSelectionMenu menu = new CharSelectionMenu(PlayerData.get(playerExact), player);

                    if (menu.isCurrentlyViewing()){
                        sender.sendMessage("Someone else is viewing this players Profiles!");
                        return true;
                    }
                    menu.open();
                }
            }
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        PlayerData playerData = PlayerData.get(player);
        if (args.length != 0){
            return false;
        }

        new CharSelectionMenu(playerData).open();
        return true;
    }
}
