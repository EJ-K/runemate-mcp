package com.runemate.mcp.cache;

import java.util.regex.*;

public final class JagTags {

    private static final Pattern NBSP_PATTERN = Pattern.compile("\u00A0");
    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("<br\\s*?/?>");
    private static final Pattern JAG_TAG_REGEX = Pattern.compile("(<.*?>)|(\\s*\\(level.?\\s*?[0-9]*\\))");

    private JagTags() {
    }

    public static String remove(String input) {
        if (input == null) {
            return null;
        }
        if (input.isEmpty()) {
            return input;
        }
        input = nbspToSp(input);
        input = LINE_BREAK_PATTERN.matcher(input).replaceAll(System.lineSeparator());
        input = JAG_TAG_REGEX.matcher(input).replaceAll("");
        return input.trim();
    }

    /**
     * Converts non-breaking spaces into regular spaces.
     */
    public static String nbspToSp(String input) {
        return NBSP_PATTERN.matcher(input).replaceAll(" ");
    }
}
