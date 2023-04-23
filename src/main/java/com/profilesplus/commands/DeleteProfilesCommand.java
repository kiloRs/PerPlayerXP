package com.profilesplus.commands;

import com.profilesplus.ProfilesPlus;
import com.profilesplus.menu.ProfileRemoveMenu;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeleteProfilesCommand implements CommandExecutor {

    private final ProfilesPlus plugin;

    public DeleteProfilesCommand(ProfilesPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (args.length == 0){
            return false;
        }
        int amount = Integer.parseInt(args[0]);
        if (amount<=0 || amount>=16){
            sender.sendMessage("Incorrect Amount!");
            return true;
        }
        PlayerData playerData = PlayerData.get(player);

        Profile profile = playerData.getProfiles().get(amount);

        if (profile == null){
            sender.sendMessage("No Profile on "+ amount);
            return true;
        }
        new ProfileRemoveMenu(playerData,profile,null).open();
        return true;
    }
}
