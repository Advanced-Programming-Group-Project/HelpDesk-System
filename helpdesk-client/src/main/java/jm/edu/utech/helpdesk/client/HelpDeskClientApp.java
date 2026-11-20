package jm.edu.utech.helpdesk.client;
// Member 1 (Front-End)

import javafx.application.Application;
import javafx.stage.Stage;

import jm.edu.utech.helpdesk.client.api.HelpDeskApi;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;

// JavaFX entry point: University Help Desk & Ticketing System client
// run with: mvn javafx:run
public class HelpDeskClientApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setMinWidth(460);
        stage.setMinHeight(520);
        Alerts.setOwner(stage);
        Navigator.init(stage);
        Navigator.showLogin();
    }

    @Override
    public void stop() {
        // closing the window while logged in counts as a logout
        Navigator.disposeCurrent();
        if (Session.isLoggedIn()) {
            try {
                HelpDeskApi.get().logout(Session.getUserId());
            } catch (RuntimeException ignored) {
                // server may be down; the app is closing anyway
            }
        }
        Background.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
