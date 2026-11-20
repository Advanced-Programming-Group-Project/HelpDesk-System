package jm.edu.utech.helpdesk.client.util;
// Member 1 (Front-End)

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextInputControl;

// checks form input before anything is sent to the server.
// rules match the backend's InputValidator so the user sees problems straight away.
// usage:
//   FormValidator v = new FormValidator();
//   v.username(usernameField); v.password(passwordField);
//   if (!v.isValid()) { v.showErrors(); return; }
public class FormValidator {

    public static final String USERNAME_PATTERN = "[A-Za-z0-9._]{3,30}";
    public static final String EMAIL_PATTERN = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";

    private static final String ERROR_STYLE_CLASS = "field-error";

    private final List<String> errors = new ArrayList<>();

    public FormValidator required(TextInputControl field, String name, int maxLength) {
        clear(field);
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            fail(field, name + " is required.");
        } else if (value.length() > maxLength) {
            fail(field, name + " must be " + maxLength + " characters or less (currently " + value.length() + ").");
        }
        return this;
    }

    public FormValidator username(TextInputControl field) {
        clear(field);
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            fail(field, "Username is required.");
        } else if (!value.matches(USERNAME_PATTERN)) {
            fail(field, "Username must be 3-30 characters (letters, numbers, dots or underscores).");
        }
        return this;
    }

    public FormValidator email(TextInputControl field) {
        clear(field);
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            fail(field, "Email is required.");
        } else if (value.length() > 100 || !value.matches(EMAIL_PATTERN)) {
            fail(field, "Email address is not valid.");
        }
        return this;
    }

    // login only needs the password to be filled in
    public FormValidator notEmpty(TextInputControl field, String name) {
        clear(field);
        if (field.getText() == null || field.getText().isEmpty()) {
            fail(field, name + " is required.");
        }
        return this;
    }

    // new passwords: 8+ characters with a letter and a number
    public FormValidator password(TextInputControl field) {
        clear(field);
        String value = field.getText() == null ? "" : field.getText();
        if (value.length() < 8) {
            fail(field, "Password must be at least 8 characters.");
        } else if (!value.matches(".*[A-Za-z].*") || !value.matches(".*[0-9].*")) {
            fail(field, "Password must contain at least one letter and one number.");
        }
        return this;
    }

    public FormValidator matches(TextInputControl field, TextInputControl original, String message) {
        clear(field);
        if (!field.getText().equals(original.getText())) {
            fail(field, message);
        }
        return this;
    }

    public FormValidator selected(ComboBox<?> box, String name) {
        clear(box);
        if (box.getValue() == null) {
            fail(box, "Please choose a " + name.toLowerCase() + ".");
        }
        return this;
    }

    // optional whole-number field (e.g. ticket ID search)
    public FormValidator optionalId(TextInputControl field, String name) {
        clear(field);
        String value = field.getText() == null ? "" : field.getText().trim();
        if (!value.isEmpty() && !value.matches("[1-9][0-9]{0,17}")) {
            fail(field, name + " must be a positive whole number.");
        }
        return this;
    }

    public FormValidator dateRange(DatePicker from, DatePicker to) {
        clear(from);
        clear(to);
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();
        if (start != null && end != null && start.isAfter(end)) {
            fail(from, "The 'from' date must be on or before the 'to' date.");
            to.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        return this;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void showErrors() {
        Alerts.warning("Please Check the Form", String.join("\n", errors));
    }

    private void fail(Control control, String message) {
        errors.add("• " + message);
        if (!control.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            control.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    private static void clear(Control control) {
        control.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    // parses an optional ID field that has already passed optionalId()
    public static Long parseId(TextInputControl field) {
        String value = field.getText() == null ? "" : field.getText().trim();
        return value.isEmpty() ? null : Long.valueOf(value);
    }
}
