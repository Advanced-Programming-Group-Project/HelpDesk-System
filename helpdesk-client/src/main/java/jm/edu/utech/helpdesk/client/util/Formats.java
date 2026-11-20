package jm.edu.utech.helpdesk.client.util;
// Member 1 (Front-End)

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// display formatting for the tables
public final class Formats {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");

    private Formats() {
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    // IN_PROGRESS -> In Progress
    public static String label(Enum<?> value) {
        if (value == null) {
            return "";
        }
        String[] words = value.name().toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    public static String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
