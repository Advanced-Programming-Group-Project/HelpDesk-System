package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import jm.edu.utech.helpdesk.client.api.ApiException;
import jm.edu.utech.helpdesk.client.api.HelpDeskApi;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.FormValidator;
import jm.edu.utech.helpdesk.client.view.AccountView;

// handles the "My Account" window (profile + password management)
public class AccountController {

    private final AccountView view;
    private final Consumer<UserDTO> onUpdated;
    private final HelpDeskApi api = HelpDeskApi.get();

    private AccountController(AccountView view, Stage stage, Consumer<UserDTO> onUpdated) {
        this.view = view;
        this.onUpdated = onUpdated;

        UserDTO user = Session.getUser();
        view.getUsernameLabel().setText(user.username());
        view.getRoleLabel().setText(user.role().name());
        view.getFullNameField().setText(user.fullName());
        view.getEmailField().setText(user.email());

        view.getSaveProfileButton().setOnAction(event -> saveProfile());
        view.getChangePasswordButton().setOnAction(event -> changePassword());
        view.getCloseButton().setOnAction(event -> stage.close());
    }

    public static void open(Consumer<UserDTO> onUpdated) {
        AccountView view = new AccountView();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Help Desk - My Account");
        Scene scene = new Scene(view.getRoot());
        scene.getStylesheets().add(AccountController.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        new AccountController(view, stage, onUpdated);
        stage.showAndWait();
    }

    private void saveProfile() {
        FormValidator validator = new FormValidator()
                .required(view.getFullNameField(), "Full name", 100)
                .email(view.getEmailField());
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        Long id = Session.getUserId();
        String fullName = view.getFullNameField().getText().trim();
        String email = view.getEmailField().getText().trim();

        Background.run(() -> api.updateUser(id, fullName, email), updated -> {
            onUpdated.accept(updated);
            Alerts.info("Profile Saved", "Your profile has been updated.");
        }, error -> {
            if (error instanceof ApiException e && e.getStatus() == 409) {
                Alerts.warning("Duplicate User Account", e.getMessage());
            } else {
                Alerts.error(error);
            }
        }, new Node[] {view.getSaveProfileButton()});
    }

    private void changePassword() {
        FormValidator validator = new FormValidator()
                .notEmpty(view.getCurrentPasswordField(), "Current password")
                .password(view.getNewPasswordField())
                .matches(view.getConfirmPasswordField(), view.getNewPasswordField(),
                        "The new passwords do not match.");
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        Long id = Session.getUserId();
        String current = view.getCurrentPasswordField().getText();
        String next = view.getNewPasswordField().getText();

        Background.run(() -> api.changePassword(id, current, next), response -> {
            view.getCurrentPasswordField().clear();
            view.getNewPasswordField().clear();
            view.getConfirmPasswordField().clear();
            Alerts.info("Password Changed", response.message());
        }, error -> {
            if (error instanceof ApiException e && e.getStatus() == 401) {
                Alerts.warning("Incorrect Password", e.getMessage());
            } else {
                Alerts.error(error);
            }
        }, new Node[] {view.getChangePasswordButton()});
    }
}
