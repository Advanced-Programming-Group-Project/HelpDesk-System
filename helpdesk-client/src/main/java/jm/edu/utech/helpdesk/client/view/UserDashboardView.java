package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;

// User Dashboard (student / staff): submit tickets, view own tickets and their status
public class UserDashboardView {

    private final DashboardFrame frame;

    // ticket form (used for both "new" and "edit")
    private final Label formTitle = new Label("Submit a Support Request");
    private final TextField subjectField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final ComboBox<jm.edu.utech.helpdesk.client.model.Priority> priorityBox = new ComboBox<>();
    private final Label charCount = new Label("0 / 2000");
    private final Button submitButton = new Button("Submit Ticket");
    private final Button cancelEditButton = new Button("Cancel Edit");

    // my tickets
    private final TableView<TicketDTO> table = TicketTable.create(false, true);
    private final ComboBox<TicketStatus> statusFilter = new ComboBox<>();
    private final Button detailsButton = new Button("View Details / Comments");
    private final Button editButton = new Button("Edit");
    private final Button closeTicketButton = new Button("Close Ticket");
    private final Button reopenButton = new Button("Reopen");

    public UserDashboardView() {
        // --- form ---
        formTitle.getStyleClass().add("panel-title");
        subjectField.setPromptText("Short summary, e.g. \"Cannot connect to campus Wi-Fi\"");
        descriptionArea.setPromptText("Describe the problem: what happened, where, any error messages...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(8);
        VBox.setVgrow(descriptionArea, Priority.ALWAYS);
        priorityBox.getItems().setAll(jm.edu.utech.helpdesk.client.model.Priority.values());
        priorityBox.setPromptText("Choose priority");
        priorityBox.setMaxWidth(Double.MAX_VALUE);
        charCount.getStyleClass().add("hint");

        submitButton.getStyleClass().add("primary-button");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(submitButton, Priority.ALWAYS);
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
        HBox formButtons = new HBox(8, cancelEditButton, submitButton);

        Label hint = new Label("Choose URGENT only when work is completely blocked.");
        hint.getStyleClass().add("hint");
        hint.setWrapText(true);

        VBox form = new VBox(8,
                formTitle,
                label("Subject"), subjectField,
                label("Description"), descriptionArea, charCount,
                label("Priority"), priorityBox, hint,
                formButtons);
        form.getStyleClass().add("panel");
        form.setPrefWidth(300);
        form.setMinWidth(280);

        // --- tickets ---
        Label tableTitle = new Label("My Tickets");
        tableTitle.getStyleClass().add("panel-title");
        statusFilter.getItems().add(null);   // null = all statuses
        statusFilter.getItems().addAll(TicketStatus.values());
        statusFilter.setPromptText("All statuses");
        statusFilter.setButtonCell(new StatusCell());
        statusFilter.setCellFactory(list -> new StatusCell());
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox tableHeader = new HBox(10, tableTitle, gap, new Label("Show:"), statusFilter);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        closeTicketButton.getStyleClass().add("danger-button");
        HBox actions = new HBox(8, detailsButton, editButton, reopenButton, closeTicketButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox.setVgrow(table, Priority.ALWAYS);
        VBox tickets = new VBox(10, tableHeader, table, actions);
        tickets.getStyleClass().add("panel");
        HBox.setHgrow(tickets, Priority.ALWAYS);

        HBox content = new HBox(12, form, tickets);
        frame = new DashboardFrame("My Help Desk", content);
    }

    // switches the form between "new ticket" and "edit ticket #n"
    public void setEditMode(Long ticketId) {
        boolean editing = ticketId != null;
        formTitle.setText(editing ? "Edit Ticket #" + ticketId : "Submit a Support Request");
        submitButton.setText(editing ? "Save Changes" : "Submit Ticket");
        cancelEditButton.setVisible(editing);
        cancelEditButton.setManaged(editing);
    }

    public void clearForm() {
        subjectField.clear();
        descriptionArea.clear();
        priorityBox.setValue(null);
        subjectField.getStyleClass().remove("field-error");
        descriptionArea.getStyleClass().remove("field-error");
        priorityBox.getStyleClass().remove("field-error");
        setEditMode(null);
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    // shows "All statuses" for the null entry
    static class StatusCell extends javafx.scene.control.ListCell<TicketStatus> {
        @Override
        protected void updateItem(TicketStatus item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item == null ? "All statuses" : jm.edu.utech.helpdesk.client.util.Formats.label(item));
        }
    }

    public DashboardFrame getFrame() {
        return frame;
    }

    public Parent getRoot() {
        return frame.getRoot();
    }

    public TextField getSubjectField() {
        return subjectField;
    }

    public TextArea getDescriptionArea() {
        return descriptionArea;
    }

    public ComboBox<jm.edu.utech.helpdesk.client.model.Priority> getPriorityBox() {
        return priorityBox;
    }

    public Label getCharCount() {
        return charCount;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public Button getCancelEditButton() {
        return cancelEditButton;
    }

    public TableView<TicketDTO> getTable() {
        return table;
    }

    public ComboBox<TicketStatus> getStatusFilter() {
        return statusFilter;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }

    public Button getEditButton() {
        return editButton;
    }

    public Button getCloseTicketButton() {
        return closeTicketButton;
    }

    public Button getReopenButton() {
        return reopenButton;
    }
}
