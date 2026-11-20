package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
// input checks
public class InputValidator {

    public static String required(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be " + maxLength + " characters or less.");
        }
        return trimmed;
    }

    public static <T> T required(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    public static String username(String username) {
        String value = required(username, "Username", 30);
        if (!value.matches("[A-Za-z0-9._]{3,30}")) {
            throw new IllegalArgumentException(
                    "Username must be 3-30 characters (letters, numbers, dots or underscores).");
        }
        return value;
    }

    public static String email(String email) {
        String value = required(email, "Email", 100);
        if (!value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email address is not valid.");
        }
        return value.toLowerCase();
    }

    public static void password(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number.");
        }
    }
}
