package org.cloudplugins.cloudJail.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

public final class ColorService {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(?i)&[0-9a-fk-or]");


    public static Component processLegacy(String message) {
        return LEGACY.deserialize(message);
    }

    public static Component processMiniMessage(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static Component processPlain(String message) {
        return PLAIN.deserialize(message);
    }

    public static String toPlain(Component message){
        return PLAIN.serialize(message);
    }

    public static boolean isBlankIgnoringLegacyCodes(String raw) {
        if (raw == null) {
            return true;
        }
        return LEGACY_CODE_PATTERN.matcher(raw).replaceAll("").trim().isEmpty();
    }

}
