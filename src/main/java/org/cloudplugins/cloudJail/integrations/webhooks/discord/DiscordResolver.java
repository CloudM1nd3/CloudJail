package org.cloudplugins.cloudJail.integrations.webhooks.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public class DiscordResolver {
    private static final int MAX_VALUE_LENGTH = 1000;

    private DiscordResolver() {}

    public static JsonObject resolve(JsonObject template, Map<String, String> values) {
        Map<String, String> prepared = new HashMap<>();
        values.forEach((k, v) -> prepared.put(k, truncate(v)));

        JsonObject result = walk(template, prepared).getAsJsonObject();

        if (!result.has("allowed_mentions")) {
            JsonObject mentions = new JsonObject();
            mentions.add("parse", new JsonArray());
            result.add("allowed_mentions", mentions);
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
}
