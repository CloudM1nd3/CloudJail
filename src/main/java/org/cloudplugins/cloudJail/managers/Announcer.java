package org.cloudplugins.cloudJail.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.integrations.webhooks.WebhookManager;
import org.cloudplugins.cloudJail.objects.AnnounceType;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;

import java.util.UUID;

public class Announcer {
    private static Announcer instance;
    private final CloudJail plugin;

    private boolean chatAnnounceEnable;
    private String announcePermission;

    public static void init(CloudJail plugin) {
        if (instance != null) {
            throw new IllegalStateException("Announcer already initialized");
        }
        instance = new Announcer(plugin);
        instance.loadSettings();
    }

    public static Announcer getInstance() {
        return instance;
    }

    private Announcer(CloudJail plugin) {
        this.plugin = plugin;
    }

    public void loadSettings() {
        chatAnnounceEnable = Config.getBoolean("announcements.chat.enable", true);
        announcePermission = Config.getString("announcements.chat.permission", "");
    }

    public void announce(AnnounceType type, Punishment punishment) {
        if (chatAnnounceEnable) {
            announceChat(type, punishment);
        }
        WebhookManager.send(type, punishment);
    }

    private void announceChat(AnnounceType type, Punishment punishment) {
        switch (type) {
            case JAILED -> sendChat(Messages.ANNOUNCE_JAILED, punishment.getPlayerUUID(),
                    "%player%", orDash(punishment.getPlayerName()),
                    "%jail%", orDash(punishment.getJailName()),
                    "%time%", TimeParser.format(punishment.getDuration()),
                    "%reason%", orDash(punishment.getReason()),
                    "%punisher%", orDash(punishment.getPunisherName()));
            case RELEASED -> sendChat(Messages.ANNOUNCE_RELEASED, punishment.getPlayerUUID(),
                    "%player%", orDash(punishment.getPlayerName()),
                    "%unjailer%", orDash(punishment.getUnjailer()),
                    "%unjail_reason%", orDash(punishment.getUnjailReason()));
            case EXPIRED -> sendChat(Messages.ANNOUNCE_EXPIRED, punishment.getPlayerUUID(),
                    "%player%", orDash(punishment.getPlayerName()));
        }
    }

    private void sendChat(Messages message, UUID targetUUID, String... replacements) {
        boolean everyone = announcePermission == null || announcePermission.isBlank();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (targetUUID != null && player.getUniqueId().equals(targetUUID)) {
                continue;
            }
            if (everyone || player.hasPermission(announcePermission)) {
                message.send(player, replacements);
            }
        }

        CommandSender console = plugin.getServer().getConsoleSender();
        if (everyone || console.hasPermission(announcePermission)) {
            message.send(console, replacements);
        }
    }

    private static String orDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }
}
