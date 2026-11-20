package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import jm.edu.utech.helpdesk.client.api.ApiException;
import jm.edu.utech.helpdesk.client.api.HelpDeskApi;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.FormValidator;
import jm.edu.utech.helpdesk.client.view.RegisterView;

// handles the registration window
public class RegisterController {

    private final RegisterView view;
    private final Stage stage;

    private RegisterController(RegisterView view, Stage stage) {
        this.view = view;
        this.stage = stage;
        view.getRegisterButton().setOnAction(event -> register());
        view.getCancelButton().setOnAction(event -> stage.close());
    }

    // opens the registration form as a modal window
    public static void open() {
        RegisterView view = new RegisterView();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Help Desk - Register");
        Scene scene = new Scene(view.getRoot());
        scene.getStylesheets().add(RegisterController.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        new RegisterController(view, stage);
        stage.showAndWait();
    }

    private void register() {
        FormValidator validator = new FormValidator()
                .required(view.getFullNameField(), "Full name", 100)
                .email(view.getEmailField())
                .username(view.getUsernameField())
                .password(view.getPasswordField())
                .matches(view.getConfirmField(), view.getPasswordField(), "The two passwords do not match.");
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }

        String fullName = view.getFullNameField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        Background.run(() -> HelpDeskApi.get().register(username, password, fullName, email),
                user -> {
                    Alerts.info("Account Created",
                            "Welcome, " + user.fullName() + "! You can now log in as '" + user.username() + "'.");
                    stage.close();
                },
                error -> {
                    if (error instanceof ApiException api && api.getStatus() == 409) {
                        Alerts.warning("Duplicate User Account", api.getMessage());
                    } else {
                        Alerts.error(error);
                    }
                },
                new javafx.scene.Node[] {view.getRegisterButton()});
    }
}
