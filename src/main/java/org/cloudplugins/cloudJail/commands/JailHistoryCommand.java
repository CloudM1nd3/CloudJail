package org.cloudplugins.cloudJail.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;

import java.util.ArrayList;
import java.util.List;

public class JailHistoryCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cloudjail.history")) {
            Messages.NO_PERMISSION.send(sender);
            return true;
        }

        if (args.length < 1) {
            Messages.USAGE_HISTORY.send(sender);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) {
            Messages.PLAYER_NOT_FOUND.send(sender, "%player%", args[0]);
            return true;
        }

        int parsed = 1;
        if (args.length > 1) {
            try {
                parsed = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Messages.USAGE_HISTORY.send(sender);
                return true;
            }
        }
        final int page = Math.max(1, parsed);

        PunishmentManager.getInstance().getHistory(target.getUniqueId(), page, result -> {
            Punishment active = result.active();

            if (result.total() == 0 && active == null) {
                Messages.HISTORY_EMPTY.send(sender, "%player%", args[0]);
                return;
            }

            if (page > Math.max(1, result.totalPages())) {
                Messages.HISTORY_PAGE_INVALID.send(sender,
                        "%page%", String.valueOf(page),
                        "%pages%", String.valueOf(Math.max(1, result.totalPages())));
                return;
            }

            Messages.HISTORY_HEADER.send(sender,
                    "%player%", args[0],
                    "%page%", String.valueOf(page),
                    "%pages%", String.valueOf(Math.max(1, result.totalPages())));

            List<Punishment> toShow = new ArrayList<>();
            if (page == 1 && active != null) {
                toShow.add(active);
            }
            toShow.addAll(result.entries());

            for (Punishment p : toShow) {
                String remainingTime = "";

                if (p == active) {
                    boolean online = Bukkit.getPlayer(p.getPlayerUUID()) != null;
                    if (online || Config.getBoolean("offline-counting", false)) {
                        p.updateRemainingTime();
                    }
                    remainingTime = Messages.HISTORY_REMAINING_TIME.get(
                            "%time%", TimeParser.format(p.getRemainingTime()));
                }

                Messages.HISTORY_LINES.sendList(sender,
                        "%id%", String.valueOf(p.getId()),
                        "%jail%", p.getJailName(),
                        "%status%", p.getStatus().name(),
                        "%time%", TimeParser.format(p.getDuration()),
                        "%remaining%", remainingTime,
                        "%punisher%", p.getPunisher(),
                        "%reason%", String.valueOf(p.getReason()));
            }
        });
        return true;
    }
}
