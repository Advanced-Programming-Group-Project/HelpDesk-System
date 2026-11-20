package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;

// Technician Dashboard: assigned tickets (auto-refreshing), status updates, resolution notes
public class TechnicianDashboardView {

    private final DashboardFrame frame;

    private final TableView<TicketDTO> table = TicketTable.create(true, false);
    private final CheckBox openOnlyBox = new CheckBox("Open only");
    private final CheckBox autoRefreshBox = new CheckBox("Auto-refresh");
    private final Label countLabel = new Label();

    // work panel for the selected ticket
    private final Label selectedLabel = new Label("Select a ticket");
    private final Label selectedInfo = new Label();
    private final ComboBox<TicketStatus> statusBox = new ComboBox<>();
    private final Button updateStatusButton = new Button("Update Status");
    private final TextArea resolutionArea = new TextArea();
    private final Button resolveButton = new Button("Mark as Resolved");
    private final Button detailsButton = new Button("View Details / Comments");

    public TechnicianDashboardView(int refreshSeconds) {
        // --- table ---
        Label tableTitle = new Label("Assigned Tickets");
        tableTitle.getStyleClass().add("panel-title");
        countLabel.getStyleClass().add("hint");
        openOnlyBox.setSelected(true);
        autoRefreshBox.setSelected(true);
        autoRefreshBox.setText("Auto-refresh (" + refreshSeconds + "s)");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox tableHeader = new HBox(12, tableTitle, countLabel, gap, openOnlyBox, autoRefreshBox);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(table, Priority.ALWAYS);
        VBox tickets = new VBox(10, tableHeader, table);
        tickets.getStyleClass().add("panel");
        HBox.setHgrow(tickets, Priority.ALWAYS);

        // --- work panel ---
        selectedLabel.getStyleClass().add("panel-title");
        selectedLabel.setWrapText(true);
        selectedInfo.setWrapText(true);
        selectedInfo.getStyleClass().add("hint");

        statusBox.getItems().setAll(TicketStatus.IN_PROGRESS, TicketStatus.PENDING);
        statusBox.setPromptText("New status");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(statusBox, Priority.ALWAYS);
        HBox statusRow = new HBox(8, statusBox, updateStatusButton);

        resolutionArea.setPromptText("What was done to fix the problem? (required to resolve)");
        resolutionArea.setWrapText(true);
        resolutionArea.setPrefRowCount(7);
        VBox.setVgrow(resolutionArea, Priority.ALWAYS);
        resolveButton.getStyleClass().add("primary-button");
        resolveButton.setMaxWidth(Double.MAX_VALUE);
        detailsButton.setMaxWidth(Double.MAX_VALUE);

        Label pendingHint = new Label("Use PENDING while waiting on the user or parts.");
        pendingHint.getStyleClass().add("hint");
        pendingHint.setWrapText(true);

        VBox work = new VBox(8,
                selectedLabel, selectedInfo, detailsButton,
                new Separator(),
                label("Update Status"), statusRow, pendingHint,
                new Separator(),
                label("Resolution Notes"), resolutionArea, resolveButton);
        work.getStyleClass().add("panel");
        work.setPrefWidth(300);
        work.setMinWidth(260);

        HBox content = new HBox(12, tickets, work);
        frame = new DashboardFrame("Technician Dashboard", content);
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    public DashboardFrame getFrame() {
        return frame;
    }

    public Parent getRoot() {
        return frame.getRoot();
    }

    public TableView<TicketDTO> getTable() {
        return table;
    }

    public CheckBox getOpenOnlyBox() {
        return openOnlyBox;
    }

    public CheckBox getAutoRefreshBox() {
        return autoRefreshBox;
    }

    public Label getCountLabel() {
        return countLabel;
    }

    public Label getSelectedLabel() {
        return selectedLabel;
    }

    public Label getSelectedInfo() {
        return selectedInfo;
    }

    public ComboBox<TicketStatus> getStatusBox() {
        return statusBox;
    }

    public Button getUpdateStatusButton() {
        return updateStatusButton;
    }

    public TextArea getResolutionArea() {
        return resolutionArea;
    }

    public Button getResolveButton() {
        return resolveButton;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }
}
