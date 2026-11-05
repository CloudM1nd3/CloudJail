package org.cloudplugins.cloudJail.utils;

import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParser {

    private record Unit(String label, long millis) {}

    private static final Pattern PART = Pattern.compile("\\s*(\\d+)\\s*([smhdw])", Pattern.CASE_INSENSITIVE);

    private static volatile List<Unit> units;
    private static volatile DateTimeFormatter dateFormatter;

    private TimeParser() {}

    public static void loadSettings(){
        setupDate(
                Messages.FORMAT_DATE_PATTERN.get(),
                Messages.FORMAT_DATE_TIMEZONE.get(),
                Messages.FORMAT_DATE_LOCALE.get()
        );
        setupUnits(
                Messages.FORMAT_UNIT_WEEKS.get(),
                Messages.FORMAT_UNIT_DAYS.get(),
                Messages.FORMAT_UNIT_HOURS.get(),
                Messages.FORMAT_UNIT_MINUTES.get(),
                Messages.FORMAT_UNIT_SECONDS.get()
        );
    }

    public static void setupDate(String pattern, String timezone, String locale) {
        dateFormatter = DateTimeFormatter
                .ofPattern(pattern, Locale.forLanguageTag(locale))
                .withZone(ZoneId.of(timezone));
    }

    public static void setupUnits(String weeks, String days, String hours, String minutes, String seconds) {
        units = List.of(
                new Unit(weeks, 604_800_000L),
                new Unit(days, 86_400_000L),
                new Unit(hours, 3_600_000L),
                new Unit(minutes, 60_000L),
                new Unit(seconds, 1_000L)
        );
    }

    public static long parse(String input) {
        String text = input.trim();
        Matcher matcher = PART.matcher(text);

        long total = 0;
        int end = 0;

        try {
            while (matcher.find()) {
                if (matcher.start() != end) return -1;

                long value = Long.parseLong(matcher.group(1));
                long unitMillis = unitToMillis(matcher.group(2).toLowerCase().charAt(0));
                total = Math.addExact(total, Math.multiplyExact(value, unitMillis));
                end = matcher.end();
            }
        } catch (NumberFormatException | ArithmeticException e) {
            return -1;
        }

        if (end != text.length() || total <= 0) return -1;
        return total;
    }

    private static long unitToMillis(char unit) {
        return switch (unit) {
            case 's' -> 1_000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 604_800_000L;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }

    public static String format(long millis) {
        long remaining = millis;
        StringBuilder sb = new StringBuilder();

        for (Unit unit : units) {
            long count = remaining / unit.millis();
            remaining %= unit.millis();
            if (count > 0) sb.append(count).append(unit.label()).append(' ');
        }

        if (sb.isEmpty()) {
            sb.append(0).append(units.get(units.size() - 1).label());
        }
        return sb.toString().trim();
    }

    public static String formatDate(long epochMillis) {
        return dateFormatter.format(Instant.ofEpochMilli(epochMillis));
    }
}