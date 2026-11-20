package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Login Screen (view only - LoginController handles the events)
public class LoginView {

    private final StackPane root = new StackPane();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button loginButton = new Button("Log In");
    private final Hyperlink registerLink = new Hyperlink("Create an account");
    private final ProgressIndicator progress = new ProgressIndicator();

    public LoginView() {
        Label title = new Label("University Help Desk");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("IT Services - Support & Ticketing");
        subtitle.getStyleClass().add("app-subtitle");

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("form-label");
        usernameField.setPromptText("e.g. jbrown");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("form-label");
        passwordField.setPromptText("Password");

        loginButton.getStyleClass().add("primary-button");
        loginButton.setDefaultButton(true);   // Enter key logs in
        loginButton.setMaxWidth(Double.MAX_VALUE);

        progress.setVisible(false);
        progress.setPrefSize(22, 22);

        Label noAccount = new Label("New student or staff member?");
        noAccount.getStyleClass().add("hint");
        registerLink.getStyleClass().add("link");
        HBox registerRow = new HBox(4, noAccount, registerLink);
        registerRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(8,
                title, subtitle,
                spacer(10),
                userLabel, usernameField,
                passLabel, passwordField,
                spacer(8),
                loginButton,
                progress,
                registerRow);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(360);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        VBox.setMargin(progress, new Insets(0, 0, 0, 160));

        root.getStyleClass().add("login-root");
        root.setPadding(new Insets(24));
        root.getChildren().add(card);
    }

    private static Label spacer(double height) {
        Label spacer = new Label();
        spacer.setMinHeight(height);
        return spacer;
    }

    public void setBusy(boolean busy) {
        progress.setVisible(busy);
        loginButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
    }

    public Parent getRoot() {
        return root;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Hyperlink getRegisterLink() {
        return registerLink;
    }
}
