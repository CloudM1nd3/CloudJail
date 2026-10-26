package org.cloudplugins.cloudJail.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;

public class JailAdminCommand implements CommandExecutor {
    private CloudJail plugin = CloudJail.getInstance();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            return false;
        }

        if (!sender.hasPermission("cloudJail.admin")) {
            Messages.NO_PERMISSION.send(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender, args);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender,args);
            case "teleport", "tp" -> handleTeleport(sender, args);
            default -> {
                return false;
            }
        }

        return true;
    }

    private void handleReload(CommandSender sender, String[] args){
        try {
            Config.reload();
            Messages.reload();
            JailManager.getInstance().reload();
            plugin.getPlayerListener().loadSettings();
            PunishmentManager.getInstance().loadSettings();
            Messages.RELOAD_SUCCESS.send(sender);
        } catch (Exception e) {
            Messages.RELOAD_FAILED.send(sender);
            plugin.getLogger().severe("Reload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //jail create <имя> [-n]
    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.ONLY_PLAYER.send(sender);
            return;
        }

        if (args.length < 2) {
            Messages.USAGE_CREATE.send(player);
            return;
        }

        String name = args[1];
        boolean noSymbols = name.matches("^[a-zA-Z0-9]+$");
        if(!noSymbols){
            Messages.JAIL_NAME_INVALID.send(player);
            return;
        }

        boolean force = args.length > 2 && args[2].equalsIgnoreCase("-n");

        try {
            if (JailManager.getInstance().createJail(name, player.getLocation(), force)) {
                Messages.JAIL_CREATED.send(sender, "%name%", name);
            } else {
                Messages.JAIL_EXISTS.send(sender, "%name%", name);
            }
        } catch (Exception e) {
            Messages.JAIL_ERROR.send(player);
            plugin.getLogger().severe("Jail creation failed: " + e.getMessage());
        }
    }

    private void handleDelete(CommandSender sender, String[] args){
        if (args.length < 2) {
            Messages.USAGE_DELETE.send(sender);
            return;
        }

        String jailName = args[1];

        try {
            if (JailManager.getInstance().deleteJail(jailName)) {
                Messages.JAIL_DELETED.send(sender, "%name%", jailName);
            } else {
                Messages.JAIL_NOT_FOUND.send(sender, "%name%", jailName);
            }
        } catch (Exception e) {
            Messages.JAIL_ERROR.send(sender);
            plugin.getLogger().severe("Jail deletion failed: " + e.getMessage());
        }
    }

    private void handleTeleport(CommandSender sender, String[] args){
        if (!(sender instanceof Player player)) {
            Messages.ONLY_PLAYER.send(sender);
            return;
        }

        if (args.length < 2) {
            Messages.USAGE_TELEPORT.send(player);
            return;
        }

        String name = args[1];

        try {
            if (JailManager.getInstance().teleportToJail(name, player)) {
                Messages.JAIL_TELEPORTATION.send(player);
            } else {
                Messages.JAIL_NOT_FOUND.send(player, "%name%", name);
            }
        } catch (Exception e) {
            Messages.JAIL_ERROR.send(player);
            plugin.getLogger().severe("Teleportation to jail failed: " + e.getMessage());
        }
    }
}
