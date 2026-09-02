package org.zalando.logbook.test;

/**
 * Utility class for ANSI color codes to format console output.
 * Stateless, containing only constants and static methods.
 */
public final class AnsiColors {

    public static final String RESET = "\u001B[0m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";

    private AnsiColors() {
        // Utility class, no instantiation
    }

    public static String cyan(String text) {
        return CYAN + text + RESET;
    }

    public static String green(String text) {
        return GREEN + text + RESET;
    }

    public static String yellow(String text) {
        return YELLOW + text + RESET;
    }
}
