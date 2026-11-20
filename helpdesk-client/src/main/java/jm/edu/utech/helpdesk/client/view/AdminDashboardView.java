package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import java.time.LocalDateTime;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import jm.edu.utech.helpdesk.client.model.Role;
import jm.edu.utech.helpdesk.client.model.TechnicianDTO;
import jm.edu.utech.helpdesk.client.model.TechnicianWorkloadDTO;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.Formats;

// Administrator Dashboard: all tickets + search + assignment, users, technicians, reports
public class AdminDashboardView {

    private final DashboardFrame frame;
    private final TabPane tabs = new TabPane();

    // ---- tickets tab ----
    private final TextField searchIdField = new TextField();
    private final ComboBox<UserDTO> searchUserBox = new ComboBox<>();
    private final ComboBox<TechnicianDTO> searchTechBox = new ComboBox<>();
    private final ComboBox<TicketStatus> searchStatusBox = new ComboBox<>();
    private final ComboBox<jm.edu.utech.helpdesk.client.model.Priority> searchPriorityBox = new ComboBox<>();
    private final DatePicker searchFrom = new DatePicker();
    private final DatePicker searchTo = new DatePicker();
    private final Button searchButton = new Button("Search");
    private final Button clearSearchButton = new Button("Clear");
    private final Label resultCount = new Label();
    private final TableView<TicketDTO> ticketTable = TicketTable.create(true, true);
    private final ComboBox<TechnicianDTO> assignTechBox = new ComboBox<>();
    private final CheckBox reassignBox = new CheckBox("Reassign");
    private final Button assignButton = new Button("Assign");
    private final Button ticketDetailsButton = new Button("View Details / Comments");
    private final Button closeTicketButton = new Button("Close");
    private final Button reopenTicketButton = new Button("Reopen");

    // ---- users tab ----
    private final TableView<UserDTO> userTable = new TableView<>();
    private final ComboBox<Role> roleBox = new ComboBox<>();
    private final Button changeRoleButton = new Button("Change Role");
    private final TextField specializationField = new TextField();
    private final Button makeTechnicianButton = new Button("Make Technician");
    private final Button toggleActiveButton = new Button("Deactivate");

    // ---- technicians tab ----
    private final TableView<TechnicianDTO> technicianTable = new TableView<>();
    private final TextField techSpecializationField = new TextField();
    private final CheckBox techAvailableBox = new CheckBox("Available for new tickets");
    private final Button saveTechnicianButton = new Button("Save Technician");

    // ---- reports tab ----
    private final DatePicker reportFrom = new DatePicker();
    private final DatePicker reportTo = new DatePicker();
    private final Button generateReportsButton = new Button("Generate Reports");
    private final Label submittedValue = statValue();
    private final Label resolvedValue = statValue();
    private final Label openValue = statValue();
    private final Label averageValue = statValue();
    private final Label averageCaption = new Label("Avg. resolution time");
    private final BarChart<String, Number> statusChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final BarChart<String, Number> priorityChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final TableView<TechnicianWorkloadDTO> workloadTable = new TableView<>();

    public AdminDashboardView() {
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Tickets", buildTicketsTab()),
                new Tab("Users", buildUsersTab()),
                new Tab("Technicians", buildTechniciansTab()),
                new Tab("Reports", buildReportsTab()));
        frame = new DashboardFrame("Administrator Dashboard", tabs);
    }

    // ======================= tickets =======================

    private Parent buildTicketsTab() {
        searchIdField.setPromptText("Ticket ID");
        searchIdField.setPrefWidth(80);
        nullable(searchUserBox, "Any user", UserDTO::toString);
        nullable(searchTechBox, "Any technician", TechnicianDTO::fullName);
        nullable(searchStatusBox, "Any status", Formats::label);
        searchStatusBox.getItems().addAll(TicketStatus.values());
        nullable(searchPriorityBox, "Any priority", Formats::label);
        searchPriorityBox.getItems().addAll(jm.edu.utech.helpdesk.client.model.Priority.values());
        searchFrom.setPromptText("From date");
        searchTo.setPromptText("To date");
        searchFrom.setPrefWidth(130);
        searchTo.setPrefWidth(130);
        searchButton.getStyleClass().add("primary-button");
        searchButton.setDefaultButton(true);

        FlowPane search = new FlowPane(8, 8,
                searchIdField, searchUserBox, searchTechBox, searchStatusBox, searchPriorityBox,
                searchFrom, searchTo, searchButton, clearSearchButton);
        search.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("All Tickets");
        title.getStyleClass().add("panel-title");
        resultCount.getStyleClass().add("hint");
        HBox titleRow = new HBox(10, title, resultCount);
        titleRow.setAlignment(Pos.BASELINE_LEFT);

        assignTechBox.setPromptText("Choose technician");
        assignTechBox.setPrefWidth(240);
        assignButton.getStyleClass().add("primary-button");
        closeTicketButton.getStyleClass().add("danger-button");
        HBox assignRow = new HBox(8, new Label("Assign to:"), assignTechBox, reassignBox, assignButton);
        assignRow.setAlignment(Pos.CENTER_LEFT);
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox actions = new HBox(8, assignRow, gap, ticketDetailsButton, reopenTicketButton, closeTicketButton);
        actions.setAlignment(Pos.CENTER_LEFT);
        assignButton.setMinWidth(Region.USE_PREF_SIZE);
        ticketDetailsButton.setMinWidth(Region.USE_PREF_SIZE);
        reopenTicketButton.setMinWidth(Region.USE_PREF_SIZE);
        closeTicketButton.setMinWidth(Region.USE_PREF_SIZE);

        VBox.setVgrow(ticketTable, Priority.ALWAYS);
        VBox box = new VBox(10, titleRow, search, ticketTable, actions);
        box.getStyleClass().add("panel");
        return pad(box);
    }

    // ======================= users =======================

    private Parent buildUsersTab() {
        userTable.setPlaceholder(new Label("No users loaded"));
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        userTable.getColumns().add(column("ID", u -> u.id(), 55));
        userTable.getColumns().add(text("Username", UserDTO::username, 120));
        userTable.getColumns().add(text("Full Name", UserDTO::fullName, 180));
        userTable.getColumns().add(text("Email", UserDTO::email, 220));
        userTable.getColumns().add(text("Role", u -> Formats.label(u.role()), 100));
        userTable.getColumns().add(text("Account", u -> u.active() ? "Active" : "Deactivated", 100));
        userTable.getColumns().add(dateColumn("Created", UserDTO::createdAt));

        roleBox.getItems().setAll(Role.USER, Role.ADMIN);   // TECHNICIAN goes through "Make Technician"
        roleBox.setPromptText("New role");
        specializationField.setPromptText("Specialization, e.g. Networking");
        specializationField.setPrefWidth(220);
        makeTechnicianButton.getStyleClass().add("primary-button");
        toggleActiveButton.getStyleClass().add("danger-button");

        Label title = new Label("Users");
        title.getStyleClass().add("panel-title");
        HBox roleRow = new HBox(8, new Label("Role:"), roleBox, changeRoleButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                specializationField, makeTechnicianButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                toggleActiveButton);
        roleRow.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(userTable, Priority.ALWAYS);
        VBox box = new VBox(10, title, userTable, roleRow);
        box.getStyleClass().add("panel");
        return pad(box);
    }

    // ======================= technicians =======================

    private Parent buildTechniciansTab() {
        technicianTable.setPlaceholder(new Label("No technicians yet - use Users > Make Technician"));
        technicianTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        technicianTable.getColumns().add(column("Tech ID", t -> t.id(), 70));
        technicianTable.getColumns().add(text("Name", TechnicianDTO::fullName, 180));
        technicianTable.getColumns().add(text("Username", TechnicianDTO::username, 120));
        technicianTable.getColumns().add(text("Email", TechnicianDTO::email, 200));
        technicianTable.getColumns().add(text("Specialization", TechnicianDTO::specialization, 150));
        technicianTable.getColumns().add(text("Available", t -> t.available() ? "Yes" : "No", 80));
        technicianTable.getColumns().add(column("Open Tickets", t -> t.activeTickets(), 100));

        techSpecializationField.setPromptText("Specialization");
        techSpecializationField.setPrefWidth(240);
        saveTechnicianButton.getStyleClass().add("primary-button");

        Label title = new Label("Technicians");
        title.getStyleClass().add("panel-title");
        HBox edit = new HBox(10, new Label("Specialization:"), techSpecializationField, techAvailableBox,
                saveTechnicianButton);
        edit.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(technicianTable, Priority.ALWAYS);
        VBox box = new VBox(10, title, technicianTable, edit);
        box.getStyleClass().add("panel");
        return pad(box);
    }

    // ======================= reports =======================

    private Parent buildReportsTab() {
        reportFrom.setPromptText("From (optional)");
        reportTo.setPromptText("To (optional)");
        generateReportsButton.getStyleClass().add("primary-button");
        Label hint = new Label("Leave the dates empty to report on all tickets. Open tickets and workload are always current.");
        hint.getStyleClass().add("hint");
        HBox controls = new HBox(8, new Label("From:"), reportFrom, new Label("To:"), reportTo, generateReportsButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(12,
                stat(submittedValue, new Label("Tickets submitted")),
                stat(resolvedValue, new Label("Tickets resolved")),
                stat(openValue, new Label("Open tickets")),
                stat(averageValue, averageCaption));

        statusChart.setTitle("Open tickets by status");
        statusChart.setLegendVisible(false);
        statusChart.setAnimated(false);
        priorityChart.setTitle("Submitted tickets by priority");
        priorityChart.setLegendVisible(false);
        priorityChart.setAnimated(false);
        statusChart.setPrefHeight(240);
        priorityChart.setPrefHeight(240);
        HBox.setHgrow(statusChart, Priority.ALWAYS);
        HBox.setHgrow(priorityChart, Priority.ALWAYS);
        HBox charts = new HBox(12, priorityChart, statusChart);

        workloadTable.setPlaceholder(new Label("Generate the reports to see technician workload"));
        workloadTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        workloadTable.getColumns().add(text("Technician", TechnicianWorkloadDTO::technicianName, 180));
        workloadTable.getColumns().add(text("Available", w -> w.available() ? "Yes" : "No", 80));
        workloadTable.getColumns().add(column("Assigned", w -> w.assigned(), 80));
        workloadTable.getColumns().add(column("In Progress", w -> w.inProgress(), 90));
        workloadTable.getColumns().add(column("Pending", w -> w.pending(), 80));
        workloadTable.getColumns().add(column("Resolved", w -> w.resolved(), 80));
        workloadTable.getColumns().add(column("Total Open", w -> w.totalOpen(), 90));
        workloadTable.setPrefHeight(180);
        VBox.setVgrow(workloadTable, Priority.ALWAYS);

        Label title = new Label("Reports");
        title.getStyleClass().add("panel-title");
        Label workloadTitle = new Label("Technician Workload");
        workloadTitle.getStyleClass().add("section-label");

        VBox box = new VBox(10, title, controls, hint, stats, charts, workloadTitle, workloadTable);
        box.getStyleClass().add("panel");
        return pad(box);
    }

    // ======================= helpers =======================

    private static Parent pad(Region content) {
        VBox wrapper = new VBox(content);
        VBox.setVgrow(content, Priority.ALWAYS);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        return wrapper;
    }

    private static Label statValue() {
        Label label = new Label("-");
        label.getStyleClass().add("stat-value");
        return label;
    }

    private static VBox stat(Label value, Label caption) {
        caption.getStyleClass().add("stat-caption");
        VBox box = new VBox(2, value, caption);
        box.getStyleClass().add("panel");
        box.setPrefWidth(200);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    // combo box whose first entry is null, shown as e.g. "Any status"
    private static <T> void nullable(ComboBox<T> box, String emptyText, Function<T, String> display) {
        box.getItems().add(null);
        box.setPromptText(emptyText);
        box.setButtonCell(new NullableCell<>(emptyText, display));
        box.setCellFactory(list -> new NullableCell<>(emptyText, display));
    }

    private static class NullableCell<T> extends ListCell<T> {
        private final String emptyText;
        private final Function<T, String> display;

        NullableCell(String emptyText, Function<T, String> display) {
            this.emptyText = emptyText;
            this.display = display;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item == null ? emptyText : display.apply(item));
        }
    }

    private static <S> TableColumn<S, String> text(String title, Function<S, String> getter, double width) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private static <S, T> TableColumn<S, T> column(String title, Function<S, T> getter, double width) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(getter.apply(c.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private static <S> TableColumn<S, String> dateColumn(String title, Function<S, LocalDateTime> getter) {
        return text(title, s -> Formats.dateTime(getter.apply(s)), 150);
    }

    // ======================= getters =======================

    public DashboardFrame getFrame() {
        return frame;
    }

    public Parent getRoot() {
        return frame.getRoot();
    }

    public TabPane getTabs() {
        return tabs;
    }

    public TextField getSearchIdField() {
        return searchIdField;
    }

    public ComboBox<UserDTO> getSearchUserBox() {
        return searchUserBox;
    }

    public ComboBox<TechnicianDTO> getSearchTechBox() {
        return searchTechBox;
    }

    public ComboBox<TicketStatus> getSearchStatusBox() {
        return searchStatusBox;
    }

    public ComboBox<jm.edu.utech.helpdesk.client.model.Priority> getSearchPriorityBox() {
        return searchPriorityBox;
    }

    public DatePicker getSearchFrom() {
        return searchFrom;
    }

    public DatePicker getSearchTo() {
        return searchTo;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getClearSearchButton() {
        return clearSearchButton;
    }

    public Label getResultCount() {
        return resultCount;
    }

    public TableView<TicketDTO> getTicketTable() {
        return ticketTable;
    }

    public ComboBox<TechnicianDTO> getAssignTechBox() {
        return assignTechBox;
    }

    public CheckBox getReassignBox() {
        return reassignBox;
    }

    public Button getAssignButton() {
        return assignButton;
    }

    public Button getTicketDetailsButton() {
        return ticketDetailsButton;
    }

    public Button getCloseTicketButton() {
        return closeTicketButton;
    }

    public Button getReopenTicketButton() {
        return reopenTicketButton;
    }

    public TableView<UserDTO> getUserTable() {
        return userTable;
    }

    public ComboBox<Role> getRoleBox() {
        return roleBox;
    }

    public Button getChangeRoleButton() {
        return changeRoleButton;
    }

    public TextField getSpecializationField() {
        return specializationField;
    }

    public Button getMakeTechnicianButton() {
        return makeTechnicianButton;
    }

    public Button getToggleActiveButton() {
        return toggleActiveButton;
    }

    public TableView<TechnicianDTO> getTechnicianTable() {
        return technicianTable;
    }

    public TextField getTechSpecializationField() {
        return techSpecializationField;
    }

    public CheckBox getTechAvailableBox() {
        return techAvailableBox;
    }

    public Button getSaveTechnicianButton() {
        return saveTechnicianButton;
    }

    public DatePicker getReportFrom() {
        return reportFrom;
    }

    public DatePicker getReportTo() {
        return reportTo;
    }

    public Button getGenerateReportsButton() {
        return generateReportsButton;
    }

    public Label getSubmittedValue() {
        return submittedValue;
    }

    public Label getResolvedValue() {
        return resolvedValue;
    }

    public Label getOpenValue() {
        return openValue;
    }

    public Label getAverageValue() {
        return averageValue;
    }

    public Label getAverageCaption() {
        return averageCaption;
    }

    public BarChart<String, Number> getStatusChart() {
        return statusChart;
    }

    public BarChart<String, Number> getPriorityChart() {
        return priorityChart;
    }

    public TableView<TechnicianWorkloadDTO> getWorkloadTable() {
        return workloadTable;
    }
}
