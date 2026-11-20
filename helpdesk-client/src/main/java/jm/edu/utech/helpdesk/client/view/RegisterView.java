package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// registration form (opened from the login screen)
public class RegisterView {

    private final VBox root = new VBox(14);
    private final TextField fullNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmField = new PasswordField();
    private final Button registerButton = new Button("Create Account");
    private final Button cancelButton = new Button("Cancel");

    public RegisterView() {
        Label title = new Label("Create an Account");
        title.getStyleClass().add("panel-title");

        fullNameField.setPromptText("Jane Brown");
        emailField.setPromptText("jane.brown@utech.edu.jm");
        usernameField.setPromptText("3-30 letters, numbers, . or _");
        passwordField.setPromptText("At least 8 characters");
        confirmField.setPromptText("Type the password again");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        ColumnConstraints labels = new ColumnConstraints();
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labels, fields);

        form.addRow(0, label("Full name"), fullNameField);
        form.addRow(1, label("Email"), emailField);
        form.addRow(2, label("Username"), usernameField);
        form.addRow(3, label("Password"), passwordField);
        form.addRow(4, label("Confirm"), confirmField);

        Label hint = new Label("Passwords need at least one letter and one number.");
        hint.getStyleClass().add("hint");

        registerButton.getStyleClass().add("primary-button");
        registerButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);
        HBox buttons = new HBox(8, cancelButton, registerButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, form, hint, buttons);
        root.setPadding(new Insets(22));
        root.setPrefWidth(440);
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    public Parent getRoot() {
        return root;
    }

    public TextField getFullNameField() {
        return fullNameField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public PasswordField getConfirmField() {
        return confirmField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}
