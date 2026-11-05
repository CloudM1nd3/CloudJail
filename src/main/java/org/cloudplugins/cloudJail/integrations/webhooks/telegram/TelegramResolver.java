package org.cloudplugins.cloudJail.integrations.webhooks.telegram;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public final class TelegramResolver {

    private static final int MAX_VALUE_LENGTH = 1000;
    private static final int MAX_TEXT_LENGTH = 4096;

    private TelegramResolver() {}

    public static JsonObject resolve(JsonObject template, Map<String, String> values, String chatId) {
        boolean html = template.has("parse_mode")
                && "HTML".equalsIgnoreCase(template.get("parse_mode").getAsString());

        Map<String, String> prepared = new HashMap<>();
        values.forEach((k, v) -> {
            String value = truncate(v);
            prepared.put(k, html ? escapeHtml(value) : value);
        });

        JsonObject result = walk(template, prepared).getAsJsonObject();
        result.addProperty("chat_id", chatId);

        if (result.has("text") && result.get("text").isJsonPrimitive()) {
            String text = result.get("text").getAsString();
            if (text.length() > MAX_TEXT_LENGTH) {
                result.addProperty("text", text.substring(0, MAX_TEXT_LENGTH));
            }
        }
        return result;
    }

    private static JsonElement walk(JsonElement node, Map<String, String> values) {
        if (node.isJsonPrimitive() && node.getAsJsonPrimitive().isString()) {
            String s = node.getAsString();
            for (Map.Entry<String, String> e : values.entrySet()) {
                s = s.replace("{" + e.getKey() + "}", e.getValue());
            }
            return new JsonPrimitive(s);
        }
        if (node.isJsonObject()) {
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> e : node.getAsJsonObject().entrySet()) {
                out.add(e.getKey(), walk(e.getValue(), values));
            }
            return out;
        }
        if (node.isJsonArray()) {
            JsonArray out = new JsonArray();
            for (JsonElement el : node.getAsJsonArray()) {
                out.add(walk(el, values));
            }
            return out;
        }
        return node;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() <= MAX_VALUE_LENGTH ? s : s.substring(0, MAX_VALUE_LENGTH - 1) + "…";
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}