package org.cloudplugins.cloudJail.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;

public class UnjailCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cloudjail.unjail")) {
            Messages.NO_PERMISSION.send(sender);
            return true;
        }
        if (args.length < 1) {
            Messages.USAGE_UNJAIL.send(sender);
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (player == null) {
            Messages.PLAYER_NOT_FOUND.send(sender, "%player%", args[0]);
            return true;
        }

        Punishment playerPunishment = PunishmentManager.getInstance().getActivePunishment(player.getUniqueId());
        if(playerPunishment == null){
            Messages.NOT_JAILED.send(sender, "%player%", player.getName());
            return true;
        }

        PunishmentManager.getInstance().release(playerPunishment, Punishment.Status.RELEASED);
        Messages.UNJAIL_SUCCESS.send(sender, "%player%", player.getName());
        return true;
    }
}
