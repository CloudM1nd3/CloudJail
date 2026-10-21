package org.cloudplugins.cloudJail.configs;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudplugins.cloudJail.utils.ColorService;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Messages {
    PREFIX("prefix", "&8[&bCloudJail&8]&r "),
    NO_PERMISSION("command.no-permission", "{prefix}&cУ вас нет прав на выполнение этой команды."),
    RELOAD_FAILED("command.reload-failed,", "{prefix}&cОшибка при перезагрузке. Смотрите в консоль."),
    RELOAD_SUCCESS("command.reload-success", "{prefix}&aКонфигурация и языковые файлы перезагружены."),
    ONLY_PLAYER("command.only-player", "{prefix}&cЭту команду можно использовать только в игре."),
    USAGE_JAIL("command.usage-jail", "{prefix}&cИспользование: /jail jail <ник>"),
    USAGE_UNJAIL("command.usage-unjail", "{prefix}&cИспользование: /jail unjail <ник>"),
    USAGE_CREATE("command.usage-create", "{prefix}&cИспользование: /jail create <имя> [-n]"),
    USAGE_DELETE("command.usage-delete", "{prefix}&cИспользование: /jail delete <имя>"),
    USAGE_TELEPORT("command.usage-teleport", "{prefix}&cИспользование: /jail tp/teleport <имя>"),


    PLAYER_NOT_FOUND("jail.player-not-found", "{prefix}&cИгрок &e%player%&c не найден!"),
    ALREADY_JAILED("jail.already-jailed", "{prefix}&cИгрок &e%player%&c уже отбывает срок под шконкой!"),
    NOT_JAILED("jail.not-jailed", "{prefix}&cИгрок не отбывает срок."),
    NO_REASON("jail.no-reason", "Потому что я могу!"),
    JAIL_TELEPORTATION("jail.teleportation", "Телепортация..."),
    JAIL_SUCCESS("jail.jail-success", "Игроку &e%player%&c порвут очко в тюрьме &e%jail%&c!"),
    UNJAIL_SUCCESS("jail.unjail-success", "{prefix}&cИгрок &e%player%&c освобождён."),
    JAIL_NAME_INVALID("jail.name-invalid", "{prefix}&cИмя тюрьма должно состоять только из букавок и/или цифарок!"),
    JAIL_CREATED("jail.created", "{prefix}&aТюрьма &e%name%&a создана."),
    JAIL_EXISTS("jail.exists", "{prefix}&cТюрьма &e%name%&c уже существует. Чтобы пересоздать, добавьте &e-n&c в конец команды."),
    JAIL_DELETED("jail.deleted", "{prefix}&aТюрьма &e%name%&a удалена."),
    JAIL_NOT_FOUND("jail.not-found", "{prefix}&cТюрьма &e%name%&c не найдена."),
    JAIL_ERROR("jail.error", "{prefix}&cПроизошла ошибка. Подробности в консоли."),

    DENY_BREAK("jail.deny.break", "{prefix}&cВ тюрьме запрещено ломать блоки."),
    DENY_PLACE("jail.deny.place", "{prefix}&cВ тюрьме запрещено ставить блоки."),
    DENY_INTERACT("jail.deny.interact", "{prefix}&cВ тюрьме запрещено использовать предметы."),
    DENY_CONSUME("jail.deny.consume", "{prefix}&cВыйди из тюрьмы, потом поешь."),
    DENY_DAMAGE("jail.deny.damage", "{prefix}&cНельзя драться, срок добавлю."),
    DENY_CHAT("jail.deny.chat", "{prefix}&cПисать в чат нельзя, учи азбуку морзе."),
    DENY_COMMAND("jail.deny.command", "{prefix}&cВ тюрьме запрещено использовать эту команду.");


    private final String path;
    private final Object defaultValue;
    private static FileConfiguration langConfig;
    private static File langFile;
    private static final Map<Messages, String> stringCache = new HashMap<>();
    private static final Map<Messages, List<String>> listCache = new HashMap<>();
    private static JavaPlugin plugin;

    Messages(String path, String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }
    Messages(String path, List<String> defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    public static void init(JavaPlugin pl) {
        plugin = pl;

        langFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!langFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                langFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("[CloudJail] Could not create messages.yml: " + e.getMessage());
            }
        }

        reload();
    }

    @SuppressWarnings("unchecked")
    public static void reload() {
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        stringCache.clear();
        listCache.clear();

        for (Messages msg : values()) {
            if(msg.defaultValue instanceof List){
                List<String> listSection = langConfig.getStringList(msg.path);
                if(listSection.isEmpty()){
                    listSection = (List<String>) msg.defaultValue;
                }
                listCache.put(msg, listSection);
            } else {
                String value = langConfig.getString(msg.path, (String) msg.defaultValue);
                stringCache.put(msg, value);
            }
        }

        boolean changed = false;
        for (Messages msg : values()) {
            if (!langConfig.contains(msg.path)) {
                langConfig.set(msg.path, msg.defaultValue);
                changed = true;
            }
        }
        if (changed) {
            save();
        }

    }

    private static void save() {
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[CloudJail] Error while saving language file: " + e.getMessage());
        }
    }

    public String get(String... replacements) {
        String message = stringCache.getOrDefault(this, (String) defaultValue);
        return applyPlaceholders(message, replacements);
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(String ... replacements){
        List<String> list = listCache.getOrDefault(this, (List<String>) defaultValue);
        if (list == null) return Collections.emptyList();

        return list.stream()
                .map(line -> applyPlaceholders(line, replacements))
                .toList();
    }

    private String applyPlaceholders(String text, String... replacements){
        String prefix = stringCache.getOrDefault(PREFIX, (String) PREFIX.defaultValue);
        text = text.replace("{prefix}", prefix);

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                text = text.replace(replacements[i], replacements[i + 1]);
            }
        }
        return text;
    }

    public void send(CommandSender sender, String... replacements) {
        if (sender == null) return;
        sender.sendMessage(ColorService.processLegacy(get(replacements)));
    }

    public void sendList(CommandSender sender, String... replacements) {
        if (sender == null) return;
        List<String> listSection = getList(replacements);
        for (String s : listSection) {
            sender.sendMessage(ColorService.processLegacy(s));
        }
    }
}