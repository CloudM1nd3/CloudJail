package org.cloudplugins.cloudJail.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.utils.TimeParser;

import java.util.Arrays;
import java.util.UUID;

public class JailCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cloudjail.jail")) {
            Messages.NO_PERMISSION.send(sender);
            return true;
        }

        if (args.length < 3) {
            Messages.USAGE_JAIL.send(sender);
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (player == null) {
            Messages.PLAYER_NOT_FOUND.send(sender, "%player%", args[0]);
            return true;
        }

        if(PunishmentManager.getInstance().getActivePunishment(player.getUniqueId()) != null){
            Messages.ALREADY_JAILED.send(sender, "%player%", player.getName());
            return true;
        }

        String jailName = args[1];
        if(!JailManager.getInstance().hasJail(jailName)){
            Messages.JAIL_NOT_FOUND.send(sender, "%name%", jailName);
            return true;
        }

        long time = TimeParser.parse(args[2]);
        if (time <= 0) {
            Messages.INVALID_TIME.send(sender);
            return true;
        }

        String reason;
        if (args.length > 3) {
            String[] subArray = Arrays.copyOfRange(args, 3, args.length);
            reason = String.join(" ", subArray);
        } else {
            reason = Messages.NO_REASON.get();
        }

        UUID punisherUUID = sender instanceof Player ? player.getUniqueId() : null;

        PunishmentManager.getInstance().jail(
                player,
                jailName,
                sender.getName(),
                punisherUUID,
                time,
                reason
        );
        Messages.JAIL_SUCCESS.send(sender, "%player%", player.getName(), "%jail%", jailName);
        return true;
    }
}
