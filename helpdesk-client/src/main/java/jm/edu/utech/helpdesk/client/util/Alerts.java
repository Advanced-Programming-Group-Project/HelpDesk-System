package jm.edu.utech.helpdesk.client.util;
// Member 1 (Front-End)

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import jm.edu.utech.helpdesk.client.api.ApiException;

// all pop-up messages in the app go through here (JavaFX Alerts)
public final class Alerts {

    private static Window owner;

    private Alerts() {
    }

    // alerts are centred on the main window
    public static void setOwner(Window window) {
        owner = window;
    }

    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void warning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    public static void error(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    // shows the right alert for a failed REST call
    public static void error(Throwable error) {
        if (error instanceof ApiException api) {
            Alert.AlertType type = api.getStatus() >= 500 || api.isConnectionError()
                    ? Alert.AlertType.ERROR : Alert.AlertType.WARNING;
            show(type, api.getTitle(), api.getMessage());
        } else {
            String message = error == null || error.getMessage() == null
                    ? "Something went wrong. Please try again." : error.getMessage();
            show(Alert.AlertType.ERROR, "Unexpected Error", message);
        }
    }

    public static boolean confirm(String title, String message) {
        Alert alert = build(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // asks for an optional line of text (e.g. a close reason); empty Optional = cancelled
    public static Optional<String> askText(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        return dialog.showAndWait().map(String::trim);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        build(type, title, message).showAndWait();
    }

    private static Alert build(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        return alert;
    }
}
