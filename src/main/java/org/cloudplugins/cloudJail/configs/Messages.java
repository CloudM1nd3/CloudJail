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
    ONLY_PLAYER("command.only-player", "{prefix}&cЭту команду можно использовать только в игре."),
    PLAYER_NOT_FOUND("jail.player-not-found", "{prefix}&cИгрок &e%player%&c не найден!"),
    RELOAD_SUCCESS("command.reload-success", "{prefix}&aКонфигурация и языковые файлы перезагружены."),
    RELOAD_FAILED("command.reload-failed", "{prefix}&cОшибка при перезагрузке. Смотрите в консоль."),

    USAGE_JAIL("command.usage-jail", "{prefix}&cИспользование: /jail <ник> <тюрьма> <время> [причина]"),
    USAGE_UNJAIL("command.usage-unjail", "{prefix}&cИспользование: /unjail <ник>"),
    USAGE_HISTORY("command.usage-history", "{prefix}&cИспользование: /jailhistory <ник> [страница]"),
    USAGE_CREATE("command.usage-create", "{prefix}&cИспользование: /jailadmin create <имя> [-n]"),
    USAGE_DELETE("command.usage-delete", "{prefix}&cИспользование: /jailadmin delete <имя>"),
    USAGE_TELEPORT("command.usage-teleport", "{prefix}&cИспользование: /jailadmin teleport <имя>"),

    JAIL_SUCCESS("jail.jail-success", "{prefix}&cИгроку &e%player%&c порвут очко в тюрьме &e%jail%&c!"),
    UNJAIL_SUCCESS("jail.unjail-success", "{prefix}&cИгрок &e%player%&c освобождён."),
    ALREADY_JAILED("jail.already-jailed", "{prefix}&cИгрок &e%player%&c уже отбывает срок под шконкой!"),
    TERM_ENDED("jail.term-ended", "{prefix}&aВаш срок в тюрьме закончен. Не нарушайте правила сервера &e[/rules]!"),
    SET_RELEASED("jail.set-released", "{prefix}&aВас выпустили из тюрьмы. Не нарушайте правила сервера &e[/rules]!"),
    SET_JAILED("jail.set-jailed", "{prefix}&cВас посадили в тюрягу. Узнать срок, причину можно в скорборде справа или по &e[/jailinfo]"),

    NOT_JAILED("jail.not-jailed", "{prefix}&e%player% &cне отбывает срок."),
    NO_REASON("jail.no-reason", "Потому что я могу!"),
    INVALID_TIME("jail.invalid-time", "{prefix}&cНеверный формат времени. Примеры: &e30s&c, &e90m&c, &e1h30m&c, &e2d&c."),

    JAIL_CREATED("jail.created", "{prefix}&aТюрьма &e%name%&a создана."),
    JAIL_EXISTS("jail.exists", "{prefix}&cТюрьма &e%name%&c уже существует. Чтобы пересоздать, добавьте &e-n&c в конец команды."),
    JAIL_DELETED("jail.deleted", "{prefix}&aТюрьма &e%name%&a удалена."),
    JAIL_NOT_FOUND("jail.not-found", "{prefix}&cТюрьма &e%name%&c не найдена."),
    JAIL_NAME_INVALID("jail.name-invalid", "{prefix}&cИмя тюрьмы должно состоять только из латинских букв и/или цифр!"),
    JAIL_TELEPORTATION("jail.teleportation", "{prefix}Телепортация..."),
    JAIL_ERROR("jail.error", "{prefix}&cПроизошла ошибка. Подробности в консоли."),

    INFO_NOT_JAILED("jail.info-not-jailed", "{prefix}&cВы не находитесь в тюрьме."),
    INFO_LINES("jail.info-lines",
            List.of("&7┌&cИнформация о вашем наказании:",
                    "&7├&cТюрьма: &e%jail%",
                    "&7├&cСрок: &e%time%",
                    "&7├&cОсталось: &e%remaining%",
                    "&7├&cАдминистратор: &e%punisher%",
                    "&7└&cПричина: &e%reason%")),

    HISTORY_EMPTY("jail.history-empty", "{prefix}&cИстория наказаний игрока &e%player%&c пуста."),
    HISTORY_HEADER("jail.history-header", "{prefix}&cИстория наказаний игрока &e%player%&c (страница &f%page%&e/&f%pages%&e):"),
    HISTORY_LINES("jail.history-lines",
            List.of("&7┌&e#%id%&c. Тюрьма - &e%jail%&c, время - &e%time% %remaining%",
                    "&7├&cАдминистратор: &e%punisher%",
                    "&7├&cПричина: &e%reason%",
                    "&7└&cСтатус: &e%status%")),
    HISTORY_REMAINING_TIME("jail.history-remaining-time", "(Осталось: &e%time%)"),
    HISTORY_PAGE_INVALID("jail.history-page-invalid", "{prefix}&cСтраницы &e%page%&c нет. Всего страниц: &e%pages%&c."),

    DENY_BREAK("jail.deny.break", "{prefix}&cВ тюрьме запрещено ломать блоки."),
    DENY_PLACE("jail.deny.place", "{prefix}&cВ тюрьме запрещено ставить блоки."),
    DENY_INTERACT("jail.deny.interact", "{prefix}&cВ тюрьме запрещено использовать предметы."),
    DENY_CONSUME("jail.deny.consume", "{prefix}&cВыйди из тюрьмы, потом поешь."),
    DENY_DAMAGE("jail.deny.damage", "{prefix}&cНельзя драться, срок добавлю."),
    DENY_CHAT("jail.deny.chat", "{prefix}&cПисать в чат нельзя, учи азбуку Морзе."),
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
            plugin.saveResource("messages.yml", false);
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