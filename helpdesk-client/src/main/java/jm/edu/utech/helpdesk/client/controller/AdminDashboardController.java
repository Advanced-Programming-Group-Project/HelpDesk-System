package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;

import jm.edu.utech.helpdesk.client.api.ApiException;
import jm.edu.utech.helpdesk.client.model.AverageResolutionTimeDTO;
import jm.edu.utech.helpdesk.client.model.Priority;
import jm.edu.utech.helpdesk.client.model.Role;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.TechnicianDTO;
import jm.edu.utech.helpdesk.client.model.TechnicianWorkloadDTO;
import jm.edu.utech.helpdesk.client.model.TicketCountReport;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.FormValidator;
import jm.edu.utech.helpdesk.client.util.Formats;
import jm.edu.utech.helpdesk.client.view.AdminDashboardView;

// handles the Administrator Dashboard
public class AdminDashboardController extends BaseDashboardController {

    private final AdminDashboardView view;
    private final Long adminId = Session.getUserId();
    private boolean searchActive;   // true while the ticket table shows search results
    private boolean reportsLoaded;

    public AdminDashboardController(AdminDashboardView view) {
        super(view.getFrame());
        this.view = view;

        // tickets tab
        view.getSearchButton().setOnAction(event -> search());
        view.getClearSearchButton().setOnAction(event -> clearSearch());
        view.getAssignButton().setOnAction(event -> withSelectedTicket(this::assign));
        view.getTicketDetailsButton().setOnAction(event -> withSelectedTicket(this::openDetails));
        view.getCloseTicketButton().setOnAction(event -> withSelectedTicket(this::closeTicket));
        view.getReopenTicketButton().setOnAction(event -> withSelectedTicket(this::reopenTicket));
        view.getTicketTable().setRowFactory(table -> {
            TableRow<TicketDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });
        view.getTicketTable().getSelectionModel().selectedItemProperty()
                .addListener((obs, old, ticket) -> updateTicketButtons(ticket));
        updateTicketButtons(null);

        // users tab
        view.getChangeRoleButton().setOnAction(event -> withSelectedUser(this::changeRole));
        view.getMakeTechnicianButton().setOnAction(event -> withSelectedUser(this::makeTechnician));
        view.getToggleActiveButton().setOnAction(event -> withSelectedUser(this::toggleActive));
        view.getUserTable().getSelectionModel().selectedItemProperty()
                .addListener((obs, old, user) -> updateUserButtons(user));
        updateUserButtons(null);

        // technicians tab
        view.getSaveTechnicianButton().setOnAction(event -> saveTechnician());
        view.getTechnicianTable().getSelectionModel().selectedItemProperty().addListener((obs, old, tech) -> {
            view.getSaveTechnicianButton().setDisable(tech == null);
            if (tech != null) {
                view.getTechSpecializationField().setText(tech.specialization());
                view.getTechAvailableBox().setSelected(tech.available());
            }
        });
        view.getSaveTechnicianButton().setDisable(true);

        // reports tab - load the first time it is opened
        view.getGenerateReportsButton().setOnAction(event -> generateReports());
        view.getTabs().getSelectionModel().selectedIndexProperty().addListener((obs, old, index) -> {
            if (index.intValue() == 3 && !reportsLoaded) {
                generateReports();
            }
        });
    }

    // ===================== loading =====================

    @Override
    protected void refresh() {
        setStatus("Loading tickets, users and technicians...");
        SearchQuery query = searchActive ? captureSearch() : null;   // read controls on the FX thread
        Background.run(() -> new AdminData(
                query != null ? runSearch(query) : api.getAllTickets(),
                api.getAllUsers(),
                api.getAllTechnicians()), data -> {
            showTickets(data.tickets());
            showUsers(data.users());
            showTechnicians(data.technicians());
            setStatus(data.tickets().size() + " ticket(s), " + data.users().size() + " user(s), "
                    + data.technicians().size() + " technician(s)");
            markUpdated();
        }, error -> {
            setStatus("Could not load data");
            Alerts.error(error);
        }, new Node[] {frame.getRefreshButton()});
    }

    private void showTickets(List<TicketDTO> tickets) {
        TicketDTO selected = view.getTicketTable().getSelectionModel().getSelectedItem();
        view.getTicketTable().getItems().setAll(tickets);
        view.getTicketTable().sort();
        view.getResultCount().setText(searchActive
                ? tickets.size() + " matching ticket(s)"
                : tickets.size() + " ticket(s)");
        if (selected != null) {
            view.getTicketTable().getItems().stream()
                    .filter(t -> t.id().equals(selected.id())).findFirst()
                    .ifPresent(t -> view.getTicketTable().getSelectionModel().select(t));
        }
    }

    private void showUsers(List<UserDTO> users) {
        UserDTO selected = view.getUserTable().getSelectionModel().getSelectedItem();
        view.getUserTable().getItems().setAll(users);
        keepValue(view.getSearchUserBox(), users, (a, b) -> a.id().equals(b.id()));
        if (selected != null) {
            users.stream().filter(u -> u.id().equals(selected.id())).findFirst()
                    .ifPresent(u -> view.getUserTable().getSelectionModel().select(u));
        }
    }

    private void showTechnicians(List<TechnicianDTO> technicians) {
        TechnicianDTO selected = view.getTechnicianTable().getSelectionModel().getSelectedItem();
        view.getTechnicianTable().getItems().setAll(technicians);
        keepValue(view.getSearchTechBox(), technicians, (a, b) -> a.id().equals(b.id()));

        // only available technicians can receive tickets
        TechnicianDTO assignChoice = view.getAssignTechBox().getValue();
        List<TechnicianDTO> available = technicians.stream().filter(TechnicianDTO::available).toList();
        view.getAssignTechBox().getItems().setAll(available);
        if (assignChoice != null) {
            available.stream().filter(t -> t.id().equals(assignChoice.id())).findFirst()
                    .ifPresent(t -> view.getAssignTechBox().setValue(t));
        }
        if (selected != null) {
            technicians.stream().filter(t -> t.id().equals(selected.id())).findFirst()
                    .ifPresent(t -> view.getTechnicianTable().getSelectionModel().select(t));
        }
    }

    // reload a "nullable" combo (first item = null) and keep its current choice
    private <T> void keepValue(ComboBox<T> box, List<T> items, java.util.function.BiPredicate<T, T> same) {
        T current = box.getValue();
        List<T> all = new ArrayList<>();
        all.add(null);
        all.addAll(items);
        box.getItems().setAll(all);
        if (current != null) {
            items.stream().filter(i -> same.test(i, current)).findFirst().ifPresent(box::setValue);
        }
    }

    // ===================== search =====================

    private void search() {
        FormValidator validator = new FormValidator()
                .optionalId(view.getSearchIdField(), "Ticket ID")
                .dateRange(view.getSearchFrom(), view.getSearchTo());
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        searchActive = true;
        SearchQuery query = captureSearch();
        Background.run(() -> runSearch(query), tickets -> {
            showTickets(tickets);
            setStatus("Search returned " + tickets.size() + " ticket(s)");
        }, new Node[] {view.getSearchButton()});
    }

    // snapshot of the search filters, taken on the FX thread
    private SearchQuery captureSearch() {
        UserDTO user = view.getSearchUserBox().getValue();
        TechnicianDTO technician = view.getSearchTechBox().getValue();
        return new SearchQuery(
                FormValidator.parseId(view.getSearchIdField()),
                user == null ? null : user.id(),
                technician == null ? null : technician.id(),
                view.getSearchStatusBox().getValue(),
                view.getSearchPriorityBox().getValue(),
                view.getSearchFrom().getValue(),
                view.getSearchTo().getValue());
    }

    // runs on a background thread
    private List<TicketDTO> runSearch(SearchQuery q) {
        return api.searchTickets(q.ticketId(), q.userId(), q.technicianId(), q.status(), q.priority(), q.from(), q.to());
    }

    private void clearSearch() {
        view.getSearchIdField().clear();
        view.getSearchUserBox().setValue(null);
        view.getSearchTechBox().setValue(null);
        view.getSearchStatusBox().setValue(null);
        view.getSearchPriorityBox().setValue(null);
        view.getSearchFrom().setValue(null);
        view.getSearchTo().setValue(null);
        view.getSearchIdField().getStyleClass().remove("field-error");
        view.getSearchFrom().getStyleClass().remove("field-error");
        view.getSearchTo().getStyleClass().remove("field-error");
        searchActive = false;
        refresh();
    }

    // ===================== ticket actions =====================

    private void assign(TicketDTO ticket) {
        TechnicianDTO technician = view.getAssignTechBox().getValue();
        if (technician == null) {
            Alerts.warning("Please Check the Form", "Choose an available technician to assign the ticket to.");
            return;
        }
        boolean reassign = view.getReassignBox().isSelected();
        Background.run(() -> api.assignTicket(ticket.id(), adminId, technician.id(), reassign), assigned -> {
            Alerts.info("Ticket Assigned", "Ticket #" + assigned.id() + " is now assigned to "
                    + assigned.assignedTechnicianName() + ".");
            view.getReassignBox().setSelected(false);
            refresh();
        }, error -> {
            // 409 = someone else assigned it first, or it is already assigned
            if (error instanceof ApiException e && e.getStatus() == 409) {
                Alerts.warning("Ticket Assignment Conflict", e.getMessage());
                refresh();
            } else {
                Alerts.error(error);
            }
        }, new Node[] {view.getAssignButton()});
    }

    private void closeTicket(TicketDTO ticket) {
        var reason = Alerts.askText("Close Ticket #" + ticket.id(), "Reason for closing (optional):");
        if (reason.isEmpty()) {
            return;
        }
        String text = reason.get().isBlank() ? null : reason.get();
        Background.run(() -> api.closeTicket(ticket.id(), adminId, text), closed -> {
            setStatus("Ticket #" + closed.id() + " closed");
            refresh();
        }, new Node[] {view.getCloseTicketButton()});
    }

    private void reopenTicket(TicketDTO ticket) {
        var reason = Alerts.askText("Reopen Ticket #" + ticket.id(), "Reason for reopening (optional):");
        if (reason.isEmpty()) {
            return;
        }
        String text = reason.get().isBlank() ? null : reason.get();
        Background.run(() -> api.reopenTicket(ticket.id(), adminId, text), reopened -> {
            setStatus("Ticket #" + reopened.id() + " reopened");
            refresh();
        }, new Node[] {view.getReopenTicketButton()});
    }

    private void openDetails(TicketDTO ticket) {
        if (TicketDetailsController.open(ticket)) {
            refresh();
        }
    }

    private void updateTicketButtons(TicketDTO ticket) {
        boolean none = ticket == null;
        view.getTicketDetailsButton().setDisable(none);
        view.getAssignButton().setDisable(none || !ticket.status().isOpen());
        view.getCloseTicketButton().setDisable(none || ticket.status() == TicketStatus.CLOSED);
        view.getReopenTicketButton().setDisable(none || ticket.status().isOpen());
    }

    // ===================== user actions =====================

    private void changeRole(UserDTO user) {
        Role role = view.getRoleBox().getValue();
        if (role == null) {
            Alerts.warning("Please Check the Form", "Choose the new role first.");
            return;
        }
        if (role == user.role()) {
            Alerts.info("No Change", user.fullName() + " is already " + Formats.label(role) + ".");
            return;
        }
        if (!Alerts.confirm("Change Role", "Change " + user.fullName() + "'s role from "
                + Formats.label(user.role()) + " to " + Formats.label(role) + "?")) {
            return;
        }
        Background.run(() -> api.changeRole(user.id(), adminId, role), updated -> {
            setStatus(updated.fullName() + " is now " + Formats.label(updated.role()));
            refresh();
        }, new Node[] {view.getChangeRoleButton()});
    }

    private void makeTechnician(UserDTO user) {
        FormValidator validator = new FormValidator().required(view.getSpecializationField(), "Specialization", 100);
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        String specialization = view.getSpecializationField().getText().trim();
        Background.run(() -> api.createTechnician(adminId, user.id(), specialization), tech -> {
            view.getSpecializationField().clear();
            Alerts.info("Technician Added", tech.fullName() + " is now a " + tech.specialization() + " technician.");
            refresh();
        }, new Node[] {view.getMakeTechnicianButton()});
    }

    private void toggleActive(UserDTO user) {
        boolean activate = !user.active();
        String action = activate ? "activate" : "deactivate";
        if (!Alerts.confirm(activate ? "Activate Account" : "Deactivate Account",
                "Are you sure you want to " + action + " " + user.fullName() + "'s account?")) {
            return;
        }
        Background.run(() -> api.setAccountStatus(user.id(), adminId, activate), updated -> {
            setStatus(updated.fullName() + "'s account " + (updated.active() ? "activated" : "deactivated"));
            refresh();
        }, new Node[] {view.getToggleActiveButton()});
    }

    private void updateUserButtons(UserDTO user) {
        boolean none = user == null;
        boolean self = !none && user.id().equals(adminId);
        view.getChangeRoleButton().setDisable(none || self);
        view.getMakeTechnicianButton().setDisable(none || user.role() != Role.USER);
        view.getToggleActiveButton().setDisable(none || self);
        view.getToggleActiveButton().setText(!none && !user.active() ? "Activate" : "Deactivate");
        view.getToggleActiveButton().getStyleClass().removeAll("danger-button", "primary-button");
        view.getToggleActiveButton().getStyleClass().add(!none && !user.active() ? "primary-button" : "danger-button");
    }

    // ===================== technician actions =====================

    private void saveTechnician() {
        TechnicianDTO tech = view.getTechnicianTable().getSelectionModel().getSelectedItem();
        if (tech == null) {
            Alerts.info("No Technician Selected", "Select a technician in the table first.");
            return;
        }
        FormValidator validator = new FormValidator()
                .required(view.getTechSpecializationField(), "Specialization", 100);
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        String specialization = view.getTechSpecializationField().getText().trim();
        boolean available = view.getTechAvailableBox().isSelected();
        Background.run(() -> api.updateTechnician(tech.id(), adminId, specialization, available), updated -> {
            setStatus("Saved technician " + updated.fullName());
            refresh();
        }, new Node[] {view.getSaveTechnicianButton()});
    }

    // ===================== reports =====================

    private void generateReports() {
        FormValidator validator = new FormValidator().dateRange(view.getReportFrom(), view.getReportTo());
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        LocalDate from = view.getReportFrom().getValue();
        LocalDate to = view.getReportTo().getValue();
        setStatus("Generating reports...");
        Background.run(() -> new Reports(
                api.ticketsSubmitted(from, to),
                api.ticketsResolved(from, to),
                api.openTickets(),
                api.averageResolutionTime(from, to),
                api.technicianWorkload()), reports -> {
            reportsLoaded = true;
            showReports(reports);
            setStatus("Reports generated");
        }, error -> {
            setStatus("Could not generate reports");
            Alerts.error(error);
        }, new Node[] {view.getGenerateReportsButton()});
    }

    private void showReports(Reports reports) {
        view.getSubmittedValue().setText(String.valueOf(reports.submitted().total()));
        view.getResolvedValue().setText(String.valueOf(reports.resolved().total()));
        view.getOpenValue().setText(String.valueOf(reports.open().total()));
        AverageResolutionTimeDTO average = reports.average();
        view.getAverageValue().setText(average.ticketsResolved() == 0 ? "-" : average.averageReadable());
        view.getAverageCaption().setText("Avg. resolution time (" + average.ticketsResolved() + " resolved)");

        fillChart(view.getPriorityChart(), reports.submitted().byPriority());
        fillChart(view.getStatusChart(), reports.open().byStatus());
        view.getWorkloadTable().getItems().setAll(reports.workload());
    }

    private static void fillChart(javafx.scene.chart.BarChart<String, Number> chart, Map<String, Long> counts) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (counts != null) {
            counts.forEach((name, count) -> series.getData().add(new XYChart.Data<>(name, count)));
        }
        chart.getData().setAll(List.of(series));
    }

    // ===================== helpers =====================

    private void withSelectedTicket(Consumer<TicketDTO> action) {
        TicketDTO ticket = view.getTicketTable().getSelectionModel().getSelectedItem();
        if (ticket == null) {
            Alerts.info("No Ticket Selected", "Select a ticket in the table first.");
            return;
        }
        action.accept(ticket);
    }

    private void withSelectedUser(Consumer<UserDTO> action) {
        UserDTO user = view.getUserTable().getSelectionModel().getSelectedItem();
        if (user == null) {
            Alerts.info("No User Selected", "Select a user in the table first.");
            return;
        }
        action.accept(user);
    }

    private record SearchQuery(Long ticketId, Long userId, Long technicianId, TicketStatus status,
                               Priority priority, LocalDate from, LocalDate to) {
    }

    private record AdminData(List<TicketDTO> tickets, List<UserDTO> users, List<TechnicianDTO> technicians) {
    }

    private record Reports(TicketCountReport submitted, TicketCountReport resolved, TicketCountReport open,
                           AverageResolutionTimeDTO average, List<TechnicianWorkloadDTO> workload) {
    }
}
