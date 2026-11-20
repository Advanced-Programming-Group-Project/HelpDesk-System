package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;

import jm.edu.utech.helpdesk.client.model.Priority;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.FormValidator;
import jm.edu.utech.helpdesk.client.view.UserDashboardView;

// handles the User Dashboard (student / staff)
public class UserDashboardController extends BaseDashboardController {

    private final UserDashboardView view;
    private List<TicketDTO> allTickets = new ArrayList<>();
    private Long editingTicketId;   // null = creating a new ticket

    public UserDashboardController(UserDashboardView view) {
        super(view.getFrame());
        this.view = view;

        view.getSubmitButton().setOnAction(event -> submit());
        view.getCancelEditButton().setOnAction(event -> cancelEdit());
        view.getDetailsButton().setOnAction(event -> withSelected(this::openDetails));
        view.getEditButton().setOnAction(event -> withSelected(this::startEdit));
        view.getCloseTicketButton().setOnAction(event -> withSelected(this::closeTicket));
        view.getReopenButton().setOnAction(event -> withSelected(this::reopenTicket));
        view.getStatusFilter().setOnAction(event -> applyFilter());

        view.getDescriptionArea().textProperty().addListener((obs, old, text) ->
                view.getCharCount().setText(text.length() + " / 2000"));

        // double-click a row to open it
        view.getTable().setRowFactory(table -> {
            TableRow<TicketDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });

        // enable only the actions that make sense for the selected ticket
        view.getTable().getSelectionModel().selectedItemProperty().addListener((obs, old, ticket) -> updateButtons(ticket));
        updateButtons(null);
    }

    @Override
    protected void refresh() {
        Long userId = Session.getUserId();
        setStatus("Loading your tickets...");
        Background.run(() -> api.getTicketsByUser(userId), tickets -> {
            allTickets = tickets;
            applyFilter();
            setStatus(tickets.size() + " ticket(s)");
            markUpdated();
        }, error -> {
            setStatus("Could not load tickets");
            Alerts.error(error);
        }, new Node[] {frame.getRefreshButton()});
    }

    private void applyFilter() {
        TicketStatus status = view.getStatusFilter().getValue();
        Long selectedId = selectedId();
        List<TicketDTO> shown = new ArrayList<>();
        for (TicketDTO ticket : allTickets) {
            if (status == null || ticket.status() == status) {
                shown.add(ticket);
            }
        }
        view.getTable().getItems().setAll(shown);
        view.getTable().sort();
        reselect(selectedId);
    }

    // ---------- create / edit ----------

    private void submit() {
        FormValidator validator = new FormValidator()
                .required(view.getSubjectField(), "Subject", 150)
                .required(view.getDescriptionArea(), "Description", 2000)
                .selected(view.getPriorityBox(), "Priority");
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }

        Long userId = Session.getUserId();
        String subject = view.getSubjectField().getText().trim();
        String description = view.getDescriptionArea().getText().trim();
        Priority priority = view.getPriorityBox().getValue();
        Long ticketId = editingTicketId;

        if (ticketId == null) {
            Background.run(() -> api.createTicket(userId, subject, description, priority), ticket -> {
                view.clearForm();
                Alerts.info("Ticket Submitted",
                        "Your ticket #" + ticket.id() + " has been submitted. IT Services will assign a technician shortly.");
                refresh();
            }, new Node[] {view.getSubmitButton()});
        } else {
            Background.run(() -> api.updateTicket(ticketId, userId, subject, description, priority), ticket -> {
                editingTicketId = null;
                view.clearForm();
                Alerts.info("Ticket Updated", "Ticket #" + ticket.id() + " has been updated.");
                refresh();
            }, new Node[] {view.getSubmitButton()});
        }
    }

    private void startEdit(TicketDTO ticket) {
        if (!ticket.status().isOpen()) {
            Alerts.warning("Cannot Edit", "Ticket #" + ticket.id() + " is " + ticket.status()
                    + ". Only open tickets can be edited - reopen it first.");
            return;
        }
        editingTicketId = ticket.id();
        view.getSubjectField().setText(ticket.subject());
        view.getDescriptionArea().setText(ticket.description());
        view.getPriorityBox().setValue(ticket.priority());
        view.setEditMode(ticket.id());
        view.getSubjectField().requestFocus();
    }

    private void cancelEdit() {
        editingTicketId = null;
        view.clearForm();
    }

    // ---------- close / reopen ----------

    private void closeTicket(TicketDTO ticket) {
        if (ticket.status() == TicketStatus.CLOSED) {
            Alerts.info("Already Closed", "Ticket #" + ticket.id() + " is already closed.");
            return;
        }
        var reason = Alerts.askText("Close Ticket #" + ticket.id(), "Reason for closing (optional):");
        if (reason.isEmpty()) {
            return;   // cancelled
        }
        Long userId = Session.getUserId();
        String text = reason.get().isBlank() ? null : reason.get();
        Background.run(() -> api.closeTicket(ticket.id(), userId, text), closed -> {
            setStatus("Ticket #" + closed.id() + " closed");
            refresh();
        }, new Node[] {view.getCloseTicketButton()});
    }

    private void reopenTicket(TicketDTO ticket) {
        if (ticket.status().isOpen()) {
            Alerts.info("Ticket Is Open", "Ticket #" + ticket.id() + " is still open.");
            return;
        }
        var reason = Alerts.askText("Reopen Ticket #" + ticket.id(), "Why does this ticket need to be reopened?");
        if (reason.isEmpty()) {
            return;
        }
        Long userId = Session.getUserId();
        String text = reason.get().isBlank() ? null : reason.get();
        Background.run(() -> api.reopenTicket(ticket.id(), userId, text), reopened -> {
            setStatus("Ticket #" + reopened.id() + " reopened");
            refresh();
        }, new Node[] {view.getReopenButton()});
    }

    // ---------- helpers ----------

    private void openDetails(TicketDTO ticket) {
        if (TicketDetailsController.open(ticket)) {
            refresh();
        }
    }

    private void updateButtons(TicketDTO ticket) {
        boolean none = ticket == null;
        view.getDetailsButton().setDisable(none);
        view.getEditButton().setDisable(none || !ticket.status().isOpen());
        view.getCloseTicketButton().setDisable(none || ticket.status() == TicketStatus.CLOSED);
        view.getReopenButton().setDisable(none || ticket.status().isOpen());
    }

    private void withSelected(java.util.function.Consumer<TicketDTO> action) {
        TicketDTO ticket = view.getTable().getSelectionModel().getSelectedItem();
        if (ticket == null) {
            Alerts.info("No Ticket Selected", "Select a ticket in the table first.");
            return;
        }
        action.accept(ticket);
    }

    private Long selectedId() {
        TicketDTO selected = view.getTable().getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.id();
    }

    private void reselect(Long id) {
        if (id == null) {
            return;
        }
        for (TicketDTO ticket : view.getTable().getItems()) {
            if (id.equals(ticket.id())) {
                view.getTable().getSelectionModel().select(ticket);
                return;
            }
        }
    }
}
