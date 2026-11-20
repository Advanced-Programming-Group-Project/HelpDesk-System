package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// layout shared by the three dashboards:
//   top    - header bar (title, signed-in user, Refresh / My Account / Log Out)
//   center - the dashboard's own content
//   right  - live notifications from the TCP notification server
//   bottom - status bar
public class DashboardFrame {

    private final BorderPane root = new BorderPane();
    private final Label userLabel = new Label();
    private final Button refreshButton = new Button("Refresh");
    private final Button accountButton = new Button("My Account");
    private final Button logoutButton = new Button("Log Out");
    private final ListView<String> notificationList = new ListView<>();
    private final Button clearNotificationsButton = new Button("Clear");
    private final Label connectionLabel = new Label("Connecting to notification server...");
    private final Label statusLabel = new Label("Ready");
    private final Label lastUpdatedLabel = new Label();

    public DashboardFrame(String title, Node content) {
        // header
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("header-title");
        userLabel.getStyleClass().add("header-user");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox header = new HBox(12, titleLabel, gap, userLabel, refreshButton, accountButton, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-bar");

        // notifications
        Label notificationTitle = new Label("Notifications");
        notificationTitle.getStyleClass().add("panel-title");
        connectionLabel.getStyleClass().add("connection-offline");
        connectionLabel.setWrapText(true);
        notificationList.getStyleClass().add("notification-list");
        notificationList.setPlaceholder(new Label("No notifications yet"));
        notificationList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setWrapText(true);
                setPrefWidth(0);   // lets the text wrap to the list width
            }
        });
        VBox.setVgrow(notificationList, Priority.ALWAYS);
        clearNotificationsButton.setMaxWidth(Double.MAX_VALUE);
        VBox notifications = new VBox(8, notificationTitle, connectionLabel, notificationList, clearNotificationsButton);
        notifications.getStyleClass().add("panel");
        notifications.setPrefWidth(230);
        notifications.setMinWidth(200);
        BorderPane.setMargin(notifications, new Insets(12, 12, 12, 0));

        // status bar
        Region statusGap = new Region();
        HBox.setHgrow(statusGap, Priority.ALWAYS);
        HBox statusBar = new HBox(statusLabel, statusGap, lastUpdatedLabel);
        statusBar.getStyleClass().add("status-bar");

        BorderPane.setMargin(content, new Insets(12));
        root.setTop(header);
        root.setCenter(content);
        root.setRight(notifications);
        root.setBottom(statusBar);
    }

    public Parent getRoot() {
        return root;
    }

    public Label getUserLabel() {
        return userLabel;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getAccountButton() {
        return accountButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public ListView<String> getNotificationList() {
        return notificationList;
    }

    public Button getClearNotificationsButton() {
        return clearNotificationsButton;
    }

    public Label getConnectionLabel() {
        return connectionLabel;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Label getLastUpdatedLabel() {
        return lastUpdatedLabel;
    }
}
