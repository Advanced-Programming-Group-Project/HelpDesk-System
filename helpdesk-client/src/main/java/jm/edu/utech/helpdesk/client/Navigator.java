package jm.edu.utech.helpdesk.client;
// Member 1 (Front-End)

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jm.edu.utech.helpdesk.client.controller.AdminDashboardController;
import jm.edu.utech.helpdesk.client.controller.LoginController;
import jm.edu.utech.helpdesk.client.controller.TechnicianDashboardController;
import jm.edu.utech.helpdesk.client.controller.UserDashboardController;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.view.AdminDashboardView;
import jm.edu.utech.helpdesk.client.view.LoginView;
import jm.edu.utech.helpdesk.client.view.TechnicianDashboardView;
import jm.edu.utech.helpdesk.client.view.UserDashboardView;

// switches between screens (one Stage, a new Scene per screen)
public final class Navigator {

    // anything that runs in the background (refresh timers, sockets) and must stop on logout
    public interface Disposable {
        void dispose();
    }

    private static Stage stage;
    private static Disposable current;

    private Navigator() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showLogin() {
        LoginView view = new LoginView();
        LoginController controller = new LoginController(view);
        show(view.getRoot(), "Help Desk - Login", 480, 560, null);
        controller.focus();
    }

    // opens the dashboard that matches the user's role
    public static void showDashboard(UserDTO user) {
        switch (user.role()) {
            case ADMIN -> {
                AdminDashboardView view = new AdminDashboardView();
                AdminDashboardController controller = new AdminDashboardController(view);
                show(view.getRoot(), "Help Desk - Administrator Dashboard", 1320, 780, controller);
                controller.start();
            }
            case TECHNICIAN -> {
                TechnicianDashboardView view =
                        new TechnicianDashboardView(TechnicianDashboardController.REFRESH_SECONDS);
                TechnicianDashboardController controller = new TechnicianDashboardController(view);
                show(view.getRoot(), "Help Desk - Technician Dashboard", 1300, 740, controller);
                controller.start();
            }
            default -> {
                UserDashboardView view = new UserDashboardView();
                UserDashboardController controller = new UserDashboardController(view);
                show(view.getRoot(), "Help Desk - My Tickets", 1300, 740, controller);
                controller.start();
            }
        }
    }

    public static void logout() {
        Session.clear();
        showLogin();
    }

    // stop background work for the screen being left
    public static void disposeCurrent() {
        if (current != null) {
            current.dispose();
            current = null;
        }
    }

    private static void show(Parent root, String title, double width, double height, Disposable controller) {
        disposeCurrent();
        current = controller;

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(Navigator.class.getResource("/styles.css").toExternalForm());

        stage.setTitle(title);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
