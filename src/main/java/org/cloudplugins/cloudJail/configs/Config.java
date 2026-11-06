package org.cloudplugins.cloudJail.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.objects.Jail;

import java.util.List;

public class Config {
    private static CloudJail plugin;
    private static FileConfiguration config;

    public static void init(CloudJail pl){
        plugin = pl;
        plugin.saveDefaultConfig();
        reload();
    }

    public static void reload(){
        plugin.reloadConfig();
        config = plugin.getConfig();

        boolean changed = addMissingDefaults();
        if (changed) {
            save();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }
    }

    public static ConfigurationSection getOrCreateSection(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            section = config.createSection(path);
        }
        return section;
    }

    public static ConfigurationSection getSection(String path){
        return config.getConfigurationSection(path);
    }

    private static boolean addMissingDefaults() {
        boolean changed = false;

        if (!config.isSet("offline-progress")) {
            config.set("offline-progress", false);
            changed = true;
        }
        if (!config.isSet("afk-progress")) {
            config.set("afk-progress", false);
            changed = true;
        }
        if (!config.isSet("autosave")) {
            config.set("autosave", 600);
            changed = true;
        }
        if(!config.isSet("keep-inventory-on-death")) {
            config.set("keep-inventory-on-death", true);
        }


        if(!config.isSet("deny.item.pick-up")) {
            config.set("deny.item.pick-up", true);
        }
        if(!config.isSet("deny.item.drop")) {
            config.set("deny.item.drop", true);
        }
        if (!config.isSet("deny.chat")) {
            config.set("deny.chat", true);
            changed = true;
        }
        if (!config.isSet("deny.consume")) {
            config.set("deny.consume", false);
            changed = true;
        }
        if (!config.isSet("deny.movement")) {
            config.set("deny.movement", false);
            changed = true;
        }
        if (!config.isSet("deny.voice-chat")) {
            config.set("deny.voice-chat", true);
            changed = true;
        }
        if (!config.isSet("deny.commands.enable")) {
            config.set("deny.commands.enable", true);
            changed = true;
        }
        if (!config.isSet("deny.commands.allowed-commands")) {
            config.set("deny.commands.allowed-commands", List.of("/jailinfo", "/rules", "/links"));
            changed = true;
        }
        if (!config.isSet("scoreboard.enable")) {
            config.set("scoreboard.enable", true);
            changed = true;
        }
        if (!config.isSet("scoreboard.title")) {
            config.set("scoreboard.title", "&6&lТюрьма");
            changed = true;
        }
        if (!config.isSet("scoreboard.lines")) {
            config.set("scoreboard.lines", List.of(
                    "&7&m----------------",
                    "&fМодератор: &e%moder%",
                    "&fПричина: &e%reason%",
                    "",
                    "&fСрок: &6%time%",
                    "&fОсталось: &a%remaining%",
                    "&7&m----------------"
            ));
            changed = true;
        }


        if (!config.isSet("Webhooks.Discord.enable")) {
            config.set("Webhooks.Discord.enable", false);
            changed = true;
        }
        if (!config.isSet("Webhooks.Discord.url")) {
            config.set("Webhooks.Discord.url", "https://discord.com/api/webhooks/ID/TOKEN");
            changed = true;
        }
        if (!config.isSet("Webhooks.Discord.templates.jailed")) {
            config.set("Webhooks.Discord.templates.jailed", """
            {
              "allowed_mentions": { "parse": [] },
              "embeds": [{
                "title": "🔒 Игрок посажен в тюрьму",
                "color": 15158332,
                "fields": [
                  { "name": "👤 Игрок", "value": "`{player}`", "inline": true },
                  { "name": "⏱ Срок", "value": "{duration}", "inline": true },
                  { "name": "🏛 Тюрьма", "value": "{jail}", "inline": true },
                  { "name": "📋 Причина", "value": "{reason}", "inline": false },
                  { "name": "👮 Выдал", "value": "`{punisher}`", "inline": false }
                ],
                "footer": { "text": "Система наказаний • {current_date}" }
              }]
            }
            """);
            changed = true;
        }
        if (!config.isSet("Webhooks.Discord.templates.released")) {
            config.set("Webhooks.Discord.templates.released", """
            {
              "allowed_mentions": { "parse": [] },
              "embeds": [{
                "title": "✅ Игрок освобождён",
                "color": 3066993,
                "fields": [
                  { "name": "👤 Игрок", "value": "`{player}`", "inline": true },
                  { "name": "🔓 Освободил", "value": "`{unjailer}`", "inline": true },
                  { "name": "📝 Причина освобождения", "value": "{unjail_reason}", "inline": false }
                ],
                "footer": { "text": "Система наказаний • {current_date}" }
              }]
            }
            """);
            changed = true;
        }
        if (!config.isSet("Webhooks.Discord.templates.expired")) {
            config.set("Webhooks.Discord.templates.expired", """
            {
              "allowed_mentions": { "parse": [] },
              "embeds": [{
                "title": "⌛ Срок наказания истёк",
                "color": 3447003,
                "fields": [
                  { "name": "👤 Игрок", "value": "`{player}`", "inline": true },
                  { "name": "📌 Статус", "value": "Автоматически освобождён, срок полностью отбыт", "inline": false }
                ],
                "footer": { "text": "Система наказаний • {current_date}" }
              }]
            }
            """);
            changed = true;
        }


        if (!config.isSet("Webhooks.Telegram.enable")) {
            config.set("Webhooks.Telegram.enable", false);
            changed = true;
        }
        if (!config.isSet("Webhooks.Telegram.bot-token")) {
            config.set("Webhooks.Telegram.bot-token", "");
            changed = true;
        }
        if (!config.isSet("Webhooks.Telegram.chat-id")) {
            config.set("Webhooks.Telegram.chat-id", "");
            changed = true;
        }
        if (!config.isSet("Webhooks.Telegram.templates.jailed")) {
            config.set("Webhooks.Telegram.templates.jailed", """
            {
              "parse_mode": "HTML",
              "text": "<b>🔒 Игрок посажен в тюрьму</b>\\n\\n┌ 👤 Игрок: <code>{player}</code>\\n├ ⏱ Срок: <b>{duration}</b>\\n├ 🏛 Тюрьма: <code>{jail}</code>\\n├ 📋 Причина: <i>{reason}</i>\\n└ 👮 Выдал: <code>{punisher}</code>\\n\\n🕒 {current_date}"
            }
            """);
            changed = true;
        }
        if (!config.isSet("Webhooks.Telegram.templates.released")) {
            config.set("Webhooks.Telegram.templates.released", """
            {
              "parse_mode": "HTML",
              "text": "<b>✅ Игрок освобождён</b>\\n\\n┌ 👤 Игрок: <code>{player}</code>\\n├ 🔓 Освободил: <code>{unjailer}</code>\\n└ 📝 Причина освобождения: <i>{unjail_reason}</i>\\n\\n🕒 {current_date}"
            }
            """);
            changed = true;
        }
        if (!config.isSet("Webhooks.Telegram.templates.expired")) {
            config.set("Webhooks.Telegram.templates.expired", """
            {
              "parse_mode": "HTML",
              "text": "<b>⌛ Срок наказания истёк</b>\\n\\n┌ 👤 Игрок: <code>{player}</code>\\n└ 📌 Статус: автоматически освобождён, срок полностью отбыт\\n\\n🕒 {current_date}"
            }
            """);
            changed = true;
        }


        return changed;
    }

    public static void setJail(String name, Jail jail){
        ConfigurationSection jailSec = getOrCreateSection("Jails." + name);

        jailSec.set("world", jail.getWorldUUID() == null ? null : jail.getWorldUUID().toString());
        jailSec.set("world-name", jail.getWorldName());
        jailSec.set("x", jail.getX());
        jailSec.set("y", jail.getY());
        jailSec.set("z", jail.getZ());
        jailSec.set("yaw", jail.getYaw());
        jailSec.set("pitch", jail.getPitch());
        save();
    }

    public static void removeJail(String name){
        config.set("Jails." + name, null);
        save();
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public static int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public static String getString(String path, String defaultValue) {
        String value = config.getString(path, defaultValue);
        return value == null ? defaultValue : value;
    }

    public static List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public static void setValue(String path, Object value) {
        config.set(path, value);
        save();
    }

    public static void save(){
        plugin.saveConfig();
    }

}
