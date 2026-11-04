package org.cloudplugins.cloudJail.integrations.webhooks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.ConfigurationSection;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.integrations.webhooks.discord.DiscordResolver;
import org.cloudplugins.cloudJail.integrations.webhooks.telegram.TelegramResolver;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class WebhookManager {
    private static WebhookManager instance;
    private final CloudJail plugin;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    private Target discord;
    private Target telegram;

    private WebhookManager(CloudJail plugin) {
        this.plugin = plugin;
    }

    public static void init(CloudJail plugin) {
        if (instance != null) throw new IllegalStateException("WebhookManager already initialized");
        instance = new WebhookManager(plugin);
        instance.loadWebhooks();
    }

    public static WebhookManager getInstance() {
        return instance;
    }

    public void loadWebhooks() {
        discord = null;
        telegram = null;

        ConfigurationSection root = Config.getSection("Webhooks");
        if (root == null) return;

        ConfigurationSection d = root.getConfigurationSection("Discord");
        if (d != null && d.getBoolean("enable", false)) {
            String url = d.getString("url", "");
            if (url.isBlank()) {
                plugin.getLogger().warning("Webhooks.Discord.url не указан, Discord отключён =(");
            } else {
                discord = new Target(url, loadTemplates(d), DiscordResolver::resolve);
            }
        }

        ConfigurationSection t = root.getConfigurationSection("Telegram");
        if (t != null && t.getBoolean("enable", false)) {
            String token = t.getString("bot-token", "");
            String chatId = t.getString("chat-id", "");
            if (token.isBlank() || chatId.isBlank()) {
                plugin.getLogger().warning("Telegram: не указан bot-token или chat-id, Telegram отключён =(");
            } else {
                telegram = new Target(
                        "https://api.telegram.org/bot" + token + "/sendMessage",
                        loadTemplates(t),
                        (tpl, vals) -> TelegramResolver.resolve(tpl, vals, chatId));
            }
        }
    }

    private Map<WebhookType, JsonObject> loadTemplates(ConfigurationSection platform) {
        Map<WebhookType, JsonObject> result = new EnumMap<>(WebhookType.class);
        ConfigurationSection tpl = platform.getConfigurationSection("templates");
        if (tpl == null) return result;

        for (WebhookType type : WebhookType.values()) {
            String raw = tpl.getString(type.name().toLowerCase(), "");
            if (raw.isBlank()) continue;
            try {
                result.put(type, JsonParser.parseString(raw).getAsJsonObject());
            } catch (Exception e) {
                plugin.getLogger().warning("Некорректный JSON в шаблоне "
                        + platform.getName() + "." + type + ": " + e.getMessage());
            }
        }
        return result;
    }

    public static void send(WebhookType type, Punishment p) {
        if (instance == null) return;
        Map<String, String> values = instance.toPlaceholders(p);
        instance.dispatch(instance.discord, type, values);
        instance.dispatch(instance.telegram, type, values);
    }

    private void dispatch(Target target, WebhookType type, Map<String, String> values) {
        if (target == null) return;
        JsonObject template = target.templates().get(type);
        if (template == null) return;

        JsonObject ready = target.resolver().apply(template, values);
        post(target.url(), ready.toString());
    }

    private Map<String, String> toPlaceholders(Punishment p) {
        Map<String, String> m = new HashMap<>();
        m.put("player", p.getPlayerName());
        m.put("punisher", p.getPunisherName());
        m.put("reason", p.getReason());
        m.put("jail", p.getJailName());
        m.put("duration", TimeParser.format(p.getDuration()));
        return m;
    }

    private void post(String url, String json) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() >= 400) {
                        plugin.getLogger().warning("Webhook вернул " + resp.statusCode() + ": " + resp.body());
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Webhook не отправлен: " + ex.getMessage());
                    return null;
                });
    }

    private static String escapeHtml(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private record Target(String url,
                          Map<WebhookType, JsonObject> templates,
                          BiFunction<JsonObject, Map<String, String>, JsonObject> resolver) {}
}
