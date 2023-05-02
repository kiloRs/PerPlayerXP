package com.profilesplus.commands;

import com.profilesplus.RPGProfiles;
import com.profilesplus.players.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()){
            return false;
        }
        if (args.length == 0){
            sender.sendMessage("- RPGProfiles Plugin Command -");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
            RPGProfiles.getInstance().reloadConfig();
            for (Map.Entry<UUID, PlayerData> entry : PlayerData.getPlayerDataInstances().entrySet()) {
                UUID uuid = entry.getKey();
                if (Bukkit.getOnlinePlayers().stream().noneMatch(player -> player.getUniqueId().equals(uuid))){
                    continue;
                }
                PlayerData playerData = entry.getValue();

                try {
                    playerData.reload();
                } catch (Exception ignored) {
                    RPGProfiles.debug("Player could not reload : " + playerData.getPlayer().getName());
                }
            }
            return true;
        }
        sender.sendMessage(MythicLib.plugin.parseColors("&7You cannot reload the plugin (Not an OP)."));
        return true;
    }
}
