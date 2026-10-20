package org.cloudplugins.cloudJail.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;

import java.util.Arrays;

public class Commands implements CommandExecutor {
    private CloudJail plugin = CloudJail.getInstance();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            return false;
        }

        if (!sender.hasPermission("cloudJail.admin")) {
            //Messages.NO_PERMISSION.send(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
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

    //jail create <имя> [-n]
    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[CloudJail] Эту команду можно использовать только в игре.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("[CloudJail] Использование: /jail create <имя> [-n]");
            return;
        }

        String name = args[1];
        boolean noSymbols = name.matches("^[a-zA-Z0-9]+$");
        if(!noSymbols){
            player.sendMessage("[CloudJail] Имя тюрьма должно состоять только из букавок и/или цифарок!");
            return;
        }

        boolean force = args.length > 2 && args[2].equalsIgnoreCase("-n");

        try {
            if (JailManager.getInstance().createJail(name, player.getLocation(), force)) {
                player.sendMessage("[CloudJail] Тюрьма " + name + " создана!");
            } else {
                player.sendMessage("[CloudJail] Тюрьма " + name + " уже существует. Чтобы пересоздать, добавь -n в конец команды.");
            }
        } catch (Exception e) {
            player.sendMessage("[CloudJail] Ошибка создания тюрьмы. Ошибка в консоли ^_^");
            plugin.getLogger().severe("Jail creation failed: " + e.getMessage());
        }
    }

    private void handleDelete(CommandSender sender, String[] args){
        if (args.length < 2) {
            sender.sendMessage("[CloudJail] Использование: /jail delete <имя>");
            return;
        }

        String jailName = args[1];

        try {
            if (JailManager.getInstance().deleteJail(jailName)) {
                sender.sendMessage("[CloudJail] Тюрьма " + jailName + " удалена.");
            } else {
                sender.sendMessage("[CloudJail] Тюрьма " + jailName + " не найдена.");
            }
        } catch (Exception e) {
            sender.sendMessage("[CloudJail] Ошибка удаления тюрьмы. Ошибка в консоли ^_^");
            plugin.getLogger().severe("Jail deletion failed: " + e.getMessage());
        }
    }

    private void handleTeleport(CommandSender sender, String[] args){
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[CloudJail] Эту команду можно использовать только в игре.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("[CloudJail] Использование: /jail teleport <имя>");
            return;
        }

        String name = args[1];

        try {
            if (JailManager.getInstance().teleportToJail(name, player)) {
                sender.sendMessage("[CloudJail] Телепортация...");
            } else {
                sender.sendMessage("[CloudJail] Тюрьма " + name + " не найдена.");
            }
        } catch (Exception e) {
            sender.sendMessage("[CloudJail] Ошибка телепортации. Ошибка в консоли ^_^");
            plugin.getLogger().severe("Teleportation to jail failed: " + e.getMessage());
        }
    }

    private void handleJail(CommandSender sender, String[] args){
        if (args.length < 3) {
            sender.sendMessage("[CloudJail] Использование: /jail jail <имя> <тюрьма> <срок> <причина>");
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if(player == null){
            sender.sendMessage("[CloudJail] Игрок " + args[1] + " не найден!");
            return;
        }

        if(PunishmentManager.getInstance().getActivePunishment(player.getUniqueId()) != null){
            sender.sendMessage("[CloudJail] Игрок " + player.getName() + " уже отбывает срок под шконкой!");
            return;
        }

        String jailName = args[2];
        if(!JailManager.getInstance().hasJail(jailName)){
            sender.sendMessage("[CloudJail] Тюрьма " + jailName + " не найдена!");
            return;
        }

        String reason;
        if (args.length > 3) {
            String[] subArray = Arrays.copyOfRange(args, 3, args.length);
            reason = String.join(" ", subArray);
        } else {
            reason = "Не указана!";
        }

        PunishmentManager.getInstance().jail(
                player,
                jailName,
                sender.getName(),
                reason,
                10000
        );
        sender.sendMessage("Игроку " + player.getName() + " порвут очко в тюрьме " + jailName + "!");
    }

    private void handleUnJail(CommandSender sender, String[] args){
        if (args.length < 2) {
            sender.sendMessage("[CloudJail] Использование: /jail unjail <имя>");
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if(player == null){
            sender.sendMessage("[CloudJail] Игрок " + args[1] + " не найден!");
            return;
        }

        Punishment playerPunishment = PunishmentManager.getInstance().getActivePunishment(player.getUniqueId());
        if(playerPunishment == null){
            sender.sendMessage("[CloudJail] Игрок " + args[1] + " не на нарах!");
            return;
        }

        PunishmentManager.getInstance().release(playerPunishment, Punishment.Status.RELEASED);
    }
}
