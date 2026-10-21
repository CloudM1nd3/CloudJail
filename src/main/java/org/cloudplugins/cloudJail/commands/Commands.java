package org.cloudplugins.cloudJail.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;
import java.util.Arrays;

public class Commands implements CommandExecutor {
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
            case "jail" -> handleJail(sender, args);
            case "unjail" -> handleUnJail(sender, args);
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

    private void handleJail(CommandSender sender, String[] args){
        if (args.length < 4) {
            Messages.USAGE_JAIL.send(sender);
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if(player == null){
            Messages.PLAYER_NOT_FOUND.send(sender, "%player%", args[1]);
            return;
        }

        if(PunishmentManager.getInstance().getActivePunishment(player.getUniqueId()) != null){
            Messages.ALREADY_JAILED.send(sender, "%player%", player.getName());
            return;
        }

        String jailName = args[2];
        if(!JailManager.getInstance().hasJail(jailName)){
            Messages.JAIL_NOT_FOUND.send(sender, "%name%", jailName);
            return;
        }

        long time = TimeParser.parse(args[3]);

        String reason;
        if (args.length > 4) {
            String[] subArray = Arrays.copyOfRange(args, 3, args.length);
            reason = String.join(" ", subArray);
        } else {
            reason = "Не указана!";
        }

        PunishmentManager.getInstance().jail(
                player,
                jailName,
                sender.getName(),
                time,
                reason
        );
        Messages.JAIL_SUCCESS.send(sender, "%player%", player.getName(), "%jail%", jailName);
    }

    private void handleUnJail(CommandSender sender, String[] args){
        if (args.length < 2) {
            Messages.USAGE_UNJAIL.send(sender);
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if(player == null){
            Messages.PLAYER_NOT_FOUND.send(sender, "%player%", args[1]);
            return;
        }

        Punishment playerPunishment = PunishmentManager.getInstance().getActivePunishment(player.getUniqueId());
        if(playerPunishment == null){
            Messages.NOT_JAILED.send(sender, "%player%", player.getName());
            return;
        }

        PunishmentManager.getInstance().release(playerPunishment, Punishment.Status.RELEASED);
        Messages.UNJAIL_SUCCESS.send(sender, "%player%", player.getName());
    }
}
