package org.cloudplugins.cloudJail.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
    private static final Pattern PART = Pattern.compile("(\\d+)([smhdw])");

    public static long parse(String input) {
        String text = input.toLowerCase();
        Matcher matcher = PART.matcher(text);

        long total = 0;
        int end = 0;

        try {
            while (matcher.find()) {
                if (matcher.start() != end) return -1;

                long value = Long.parseLong(matcher.group(1));
                total = Math.addExact(total, Math.multiplyExact(value, unitToMillis(matcher.group(2).charAt(0))));
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
            case 's' -> 1000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 604_800_000L;
            default -> 0L;
        };
    }

    public static String format(long millis) {
        long seconds = millis / 1000;
        long d = seconds / 86400;
        long h = seconds % 86400 / 3600;
        long m = seconds % 3600 / 60;
        long s = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("д ");
        if (h > 0) sb.append(h).append("ч ");
        if (m > 0) sb.append(m).append("м ");
        if (s > 0 || sb.isEmpty()) sb.append(s).append("с.");
        return sb.toString().trim();
    }
}