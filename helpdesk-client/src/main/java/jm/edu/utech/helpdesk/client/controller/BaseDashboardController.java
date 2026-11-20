package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import jm.edu.utech.helpdesk.client.Navigator;
import jm.edu.utech.helpdesk.client.api.HelpDeskApi;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.notification.NotificationListener;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.view.DashboardFrame;

// behaviour shared by the three dashboards: header buttons, logout, notifications, status bar
public abstract class BaseDashboardController implements Navigator.Disposable {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm:ss a");
    private static final int MAX_NOTIFICATIONS = 100;

    protected final HelpDeskApi api = HelpDeskApi.get();
    protected final DashboardFrame frame;
    private NotificationListener notifications;

    protected BaseDashboardController(DashboardFrame frame) {
        this.frame = frame;

        UserDTO user = Session.getUser();
        frame.getUserLabel().setText("Signed in as " + user.fullName() + " (" + user.role() + ")");
        frame.getRefreshButton().setOnAction(event -> refresh());
        frame.getAccountButton().setOnAction(event -> AccountController.open(this::onAccountUpdated));
        frame.getLogoutButton().setOnAction(event -> logout());
        frame.getClearNotificationsButton().setOnAction(event -> frame.getNotificationList().getItems().clear());
    }

    // called once the screen is showing
    public void start() {
        notifications = new NotificationListener(Session.getUser(), this::onNotification, this::onConnectionChange);
        notifications.start();
        refresh();
    }

    // reload the dashboard's data from the server
    protected abstract void refresh();

    // a notification arrived from the TCP server; dashboards may also reload
    protected void onNotification(String message) {
        frame.getNotificationList().getItems().add(0, "[" + LocalTime.now().format(TIME) + "] " + message);
        if (frame.getNotificationList().getItems().size() > MAX_NOTIFICATIONS) {
            frame.getNotificationList().getItems().remove(MAX_NOTIFICATIONS);
        }
        setStatus("New notification: " + message);
        refresh();
    }

    private void onConnectionChange(boolean online) {
        frame.getConnectionLabel().setText(online
                ? "Connected - live updates on"
                : "Notification server offline - retrying every 10s");
        frame.getConnectionLabel().getStyleClass().setAll("label", online ? "connection-online" : "connection-offline");
    }

    private void onAccountUpdated(UserDTO updated) {
        Session.setUser(updated);
        frame.getUserLabel().setText("Signed in as " + updated.fullName() + " (" + updated.role() + ")");
    }

    protected void setStatus(String text) {
        frame.getStatusLabel().setText(text);
    }

    protected void markUpdated() {
        frame.getLastUpdatedLabel().setText("Last updated " + LocalTime.now().format(TIME));
    }

    private void logout() {
        if (!Alerts.confirm("Log Out", "Are you sure you want to log out?")) {
            return;
        }
        Long userId = Session.getUserId();
        // tell the server (for its audit log); log out locally even if the server is unreachable
        Background.run(() -> api.logout(userId), ok -> Navigator.logout(), error -> Navigator.logout(), null);
    }

    @Override
    public void dispose() {
        if (notifications != null) {
            notifications.stop();
        }
    }
}
