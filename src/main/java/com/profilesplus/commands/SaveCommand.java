package com.profilesplus.commands;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SaveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()){
            if (sender instanceof Player player){
                PlayerData.get(player).saveProfiles();
                sender.sendMessage("Profiles have been saved!");
                return true;
            }
            return false;
        }
        if (args.length == 0){
        new BukkitRunnable(){
            @Override
            public void run() {
                PlayerData.getPlayerDataInstances().forEach((uuid, playerData) -> {
                    playerData.saveProfiles();
                });
            }
        }.runTask(RPGProfiles.getInstance());

        return true;
        }
        else if (args.length == 1 ){
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player == null){
                sender.sendMessage(MythicLib.plugin.parseColors("&cPlayer not available!"));
                return true;
            }
            PlayerData playerData = PlayerData.get(player);

            playerData.saveProfiles();
            sender.sendMessage(MythicLib.plugin.parseColors("&aSaved Profiles of :" + player.getName()));
            return true;
        }
        return false;
    }
}
