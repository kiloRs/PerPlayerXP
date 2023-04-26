package com.profilesplus.commands;

import com.profilesplus.RPGProfiles;
import com.profilesplus.menu.ProfileRemoveMenu;
import com.profilesplus.players.PlayerData;
import com.profilesplus.players.Profile;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class DeleteProfilesCommand implements CommandExecutor {

    private final RPGProfiles plugin;

    public DeleteProfilesCommand(RPGProfiles plugin) {
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
