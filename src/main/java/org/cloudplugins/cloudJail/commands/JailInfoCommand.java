package org.cloudplugins.cloudJail.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.listeners.PlayerListener;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;

public class JailInfoCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[CloudJail] Эта команда только для игроков.");
            return true;
        }

        Punishment p = PunishmentManager.getInstance().getActivePunishment(player.getUniqueId());
        if (p == null) {
            Messages.INFO_NOT_JAILED.send(player);
            return true;
        }

        p.updateRemainingTime();

        Messages.INFO_LINES.sendList(player,
                "%jail%", p.getJailName(),
                "%punisher%", p.getPunisher(),
                "%reason%", String.valueOf(p.getReason()),
                "%time%", TimeParser.format(p.getDuration()),
                "%remaining%", TimeParser.format(p.getRemainingTime()));
        return true;
    }
}
