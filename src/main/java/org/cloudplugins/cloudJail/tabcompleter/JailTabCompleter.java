package org.cloudplugins.cloudJail.tabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.managers.JailManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JailTabCompleter implements TabCompleter {

    private static final List<String> TIMES = List.of("10m", "30m", "1h", "6h", "1d");

    private static final List<String> ADMIN_COMMANDS = List.of("create", "delete", "tp", "reload");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();

        switch (name) {
            case "jail" -> {
                if (args.length == 1) {
                    return filter(getOnlinePlayers(), args[0]);
                } else if (args.length == 2) {
                    return filter(JailManager.getInstance().getJailNames(), args[1]);
                } else if (args.length == 3) {
                    return filter(TIMES, args[2]);
                }
                return Collections.emptyList();
            }
            case "unjail", "jailhistory", "jailpunishments" -> {
                if (args.length == 1) {
                    return filter(getOnlinePlayers(), args[0]);
                }
                return Collections.emptyList();
            }
            case "jailadmin" -> {
                if (args.length == 1) {
                    return filter(ADMIN_COMMANDS, args[0]);
                } else if (args.length == 2){
                    if(args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("delete")){
                        return filter(JailManager.getInstance().getJailNames(), args[1]);
                    }
                }
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<String> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}