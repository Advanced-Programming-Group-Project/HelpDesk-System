package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// "My Account" window: update profile and change password
public class AccountView {

    private final VBox root = new VBox(14);
    private final Label usernameLabel = new Label();
    private final Label roleLabel = new Label();
    private final TextField fullNameField = new TextField();
    private final TextField emailField = new TextField();
    private final Button saveProfileButton = new Button("Save Profile");
    private final PasswordField currentPasswordField = new PasswordField();
    private final PasswordField newPasswordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();
    private final Button changePasswordButton = new Button("Change Password");
    private final Button closeButton = new Button("Close");

    public AccountView() {
        Label profileTitle = new Label("Profile");
        profileTitle.getStyleClass().add("panel-title");

        GridPane profile = grid();
        profile.addRow(0, label("Username"), usernameLabel);
        profile.addRow(1, label("Role"), roleLabel);
        profile.addRow(2, label("Full name"), fullNameField);
        profile.addRow(3, label("Email"), emailField);
        saveProfileButton.getStyleClass().add("primary-button");
        HBox profileButtons = new HBox(saveProfileButton);
        profileButtons.setAlignment(Pos.CENTER_RIGHT);

        Label passwordTitle = new Label("Change Password");
        passwordTitle.getStyleClass().add("panel-title");
        GridPane password = grid();
        password.addRow(0, label("Current"), currentPasswordField);
        password.addRow(1, label("New"), newPasswordField);
        password.addRow(2, label("Confirm new"), confirmPasswordField);
        newPasswordField.setPromptText("At least 8 characters, a letter and a number");
        changePasswordButton.getStyleClass().add("primary-button");
        HBox passwordButtons = new HBox(changePasswordButton);
        passwordButtons.setAlignment(Pos.CENTER_RIGHT);

        closeButton.setCancelButton(true);
        HBox bottom = new HBox(closeButton);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(profileTitle, profile, profileButtons, new Separator(),
                passwordTitle, password, passwordButtons, new Separator(), bottom);
        root.setPadding(new Insets(22));
        root.setPrefWidth(460);
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints labels = new ColumnConstraints(100);
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, fields);
        return grid;
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    public Parent getRoot() {
        return root;
    }

    public Label getUsernameLabel() {
        return usernameLabel;
    }

    public Label getRoleLabel() {
        return roleLabel;
    }

    public TextField getFullNameField() {
        return fullNameField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public Button getSaveProfileButton() {
        return saveProfileButton;
    }

    public PasswordField getCurrentPasswordField() {
        return currentPasswordField;
    }

    public PasswordField getNewPasswordField() {
        return newPasswordField;
    }

    public PasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public Button getChangePasswordButton() {
        return changePasswordButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }
}
