package org.cloudplugins.cloudJail.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;

public class JailCheckCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cloudjail.history")) {
            Messages.NO_PERMISSION.send(sender);
            return true;
        }

        if (args.length < 1) {
            Messages.USAGE_JAILCHECK.send(sender);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) {
            Messages.PLAYER_NOT_FOUND.send(sender, "%player%", args[0]);
            return true;
        }

        Punishment activePunishment = PunishmentManager.getInstance().getActivePunishment(target.getUniqueId());
        if(activePunishment == null){
            Messages.NOT_JAILED.send(sender, "%player%", target.getName());
            return true;
        }

        String remainingTime = "";
        if (!activePunishment.isExpired()) {
            remainingTime = Messages.HISTORY_REMAINING_TIME.get(
                    "%time%", TimeParser.format(activePunishment.getRemainingMillis()));
        }

        Messages.HISTORY_LINES.sendList(sender,
                "%id%", String.valueOf(activePunishment.getId()),
                "%jail%", activePunishment.getJailName(),
                "%player%", activePunishment.getPlayerName(),
                "%status%", activePunishment.getStatus().getMessage().get("%unjailer%", activePunishment.getUnjailer() == null ? "" : activePunishment.getUnjailer()),
                "%date%", TimeParser.formatDate(activePunishment.getStartTimeStamp()),
                "%time%", TimeParser.format(activePunishment.getDuration()),
                "%remaining%", remainingTime,
                "%punisher%", activePunishment.getPunisherName(),
                "%reason%", String.valueOf(activePunishment.getReason()));

        return  true;
    }
}
