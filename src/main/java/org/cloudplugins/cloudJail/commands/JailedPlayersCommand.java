package org.cloudplugins.cloudJail.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;

import java.util.ArrayList;
import java.util.List;

public class JailedPlayersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cloudjail.history")) {
            Messages.NO_PERMISSION.send(sender);
            return true;
        }

        int parsed = 1;
        if (args.length > 0) {
            try {
                parsed = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                Messages.USAGE_JAILED_PLAYERS.send(sender);
                return true;
            }
        }
        final int page = Math.max(1, parsed);

        PunishmentManager.getInstance().getActive(page, result -> {
            if (result.getTotal() == 0) {
                Messages.JAILED_PLAYERS_EMPTY.send(sender);
                return;
            }

            if (page > Math.max(1, result.getTotalPages())) {
                Messages.HISTORY_PAGE_INVALID.send(sender,
                        "%page%", String.valueOf(page),
                        "%pages%", String.valueOf(Math.max(1, result.getTotalPages())));
                return;
            }

            Messages.JAILED_PLAYERS_HEADER.send(sender,
                    "%page%", String.valueOf(page),
                    "%pages%", String.valueOf(Math.max(1, result.getTotalPages())));

            List<Punishment> toShow = new ArrayList<>(result.getEntries());

            for (Punishment p : toShow) {
                String remainingTime = Messages.HISTORY_REMAINING_TIME.get("%time%", TimeParser.format(p.getRemainingMillis()));

                Messages.HISTORY_LINES.sendList(sender,
                        "%id%", String.valueOf(p.getId()),
                        "%jail%", p.getJailName(),
                        "%status%", p.getStatus().name(),
                        "%time%", TimeParser.format(p.getDuration()),
                        "%remaining%", remainingTime,
                        "%punisher%", p.getPunisherName(),
                        "%reason%", String.valueOf(p.getReason()));
            }
        });
        return true;
    }
}

