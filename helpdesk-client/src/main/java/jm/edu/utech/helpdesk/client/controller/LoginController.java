package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import javafx.application.Platform;

import jm.edu.utech.helpdesk.client.Navigator;
import jm.edu.utech.helpdesk.client.api.ApiException;
import jm.edu.utech.helpdesk.client.api.HelpDeskApi;
import jm.edu.utech.helpdesk.client.model.Role;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.FormValidator;
import jm.edu.utech.helpdesk.client.view.LoginView;

// handles the Login Screen
public class LoginController {

    private final LoginView view;
    private final HelpDeskApi api = HelpDeskApi.get();

    public LoginController(LoginView view) {
        this.view = view;
        view.getLoginButton().setOnAction(event -> login());
        view.getRegisterLink().setOnAction(event -> RegisterController.open());
    }

    public void focus() {
        Platform.runLater(() -> view.getUsernameField().requestFocus());
    }

    private void login() {
        FormValidator validator = new FormValidator()
                .notEmpty(view.getUsernameField(), "Username")
                .notEmpty(view.getPasswordField(), "Password");
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }

        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        view.setBusy(true);
        Background.run(() -> {
            UserDTO user = api.login(username, password);
            // a technician also needs their technician ID for the dashboard calls
            Long technicianId = user.role() == Role.TECHNICIAN ? api.findTechnicianIdForUser(user.id()) : null;
            return new LoginResult(user, technicianId);
        }, result -> {
            view.setBusy(false);
            if (result.user().role() == Role.TECHNICIAN && result.technicianId() == null) {
                Alerts.error("Technician Profile Missing",
                        "Your account has the technician role but no technician profile was found. "
                                + "Ask an administrator to set you up as a technician.");
                return;
            }
            Session.start(result.user(), result.technicianId());
            Navigator.showDashboard(result.user());
        }, error -> {
            view.setBusy(false);
            view.getPasswordField().clear();
            if (error instanceof ApiException api && api.getStatus() == 401) {
                Alerts.error("Invalid Login Credentials", api.getMessage());
            } else {
                Alerts.error(error);
            }
            view.getPasswordField().requestFocus();
        }, null);
    }

    private record LoginResult(UserDTO user, Long technicianId) {
    }
}
