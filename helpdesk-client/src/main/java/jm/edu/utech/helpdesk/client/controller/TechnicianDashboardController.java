package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.AppConfig;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.Formats;
import jm.edu.utech.helpdesk.client.view.TechnicianDashboardView;

// handles the Technician Dashboard.
// Background Ticket Refresh: a ScheduledService reloads the assigned tickets on a background
// thread every few seconds, so the table stays current without freezing the GUI.
public class TechnicianDashboardController extends BaseDashboardController {

    public static final int REFRESH_SECONDS = AppConfig.getInt("refresh.seconds", 15);

    private final TechnicianDashboardView view;
    private final Long technicianId = Session.getTechnicianId();
    private final ScheduledService<List<TicketDTO>> autoRefresh;
    private List<TicketDTO> allTickets = new ArrayList<>();
    private boolean refreshErrorShown;

    public TechnicianDashboardController(TechnicianDashboardView view) {
        super(view.getFrame());
        this.view = view;

        autoRefresh = new ScheduledService<>() {
            @Override
            protected Task<List<TicketDTO>> createTask() {
                return new Task<>() {
                    @Override
                    protected List<TicketDTO> call() {
                        return api.getAssignedTickets(technicianId);   // runs off the FX thread
                    }
                };
            }
        };
        autoRefresh.setPeriod(Duration.seconds(REFRESH_SECONDS));
        autoRefresh.setDelay(Duration.seconds(REFRESH_SECONDS));
        autoRefresh.setRestartOnFailure(true);   // keep trying if the server goes down for a moment
        autoRefresh.setOnSucceeded(event -> {
            refreshErrorShown = false;
            showTickets(autoRefresh.getValue());
            setStatus("Auto-refreshed " + allTickets.size() + " ticket(s)");
        });
        autoRefresh.setOnFailed(event -> {
            setStatus("Auto-refresh failed: " + autoRefresh.getException().getMessage());
            // only pop up once per outage, not every 15 seconds
            if (!refreshErrorShown) {
                refreshErrorShown = true;
                Alerts.error(autoRefresh.getException());
            }
        });

        view.getAutoRefreshBox().selectedProperty().addListener((obs, old, on) -> {
            if (on) {
                autoRefresh.restart();
                setStatus("Auto-refresh on");
            } else {
                autoRefresh.cancel();
                setStatus("Auto-refresh paused");
            }
        });
        view.getOpenOnlyBox().setOnAction(event -> applyFilter());
        view.getUpdateStatusButton().setOnAction(event -> withSelected(this::updateStatus));
        view.getResolveButton().setOnAction(event -> withSelected(this::resolve));
        view.getDetailsButton().setOnAction(event -> withSelected(this::openDetails));

        view.getTable().setRowFactory(table -> {
            TableRow<TicketDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });
        view.getTable().getSelectionModel().selectedItemProperty().addListener((obs, old, ticket) -> showSelected(ticket));
        showSelected(null);
    }

    @Override
    public void start() {
        super.start();
        autoRefresh.start();
    }

    @Override
    protected void refresh() {
        setStatus("Loading assigned tickets...");
        Background.run(() -> api.getAssignedTickets(technicianId), tickets -> {
            showTickets(tickets);
            setStatus(tickets.size() + " assigned ticket(s)");
        }, error -> {
            setStatus("Could not load tickets");
            Alerts.error(error);
        }, new Node[] {frame.getRefreshButton()});
    }

    private void showTickets(List<TicketDTO> tickets) {
        allTickets = tickets;
        applyFilter();
        markUpdated();
    }

    private void applyFilter() {
        boolean openOnly = view.getOpenOnlyBox().isSelected();
        TicketDTO selected = view.getTable().getSelectionModel().getSelectedItem();
        Long selectedId = selected == null ? null : selected.id();

        List<TicketDTO> shown = new ArrayList<>();
        long open = 0;
        for (TicketDTO ticket : allTickets) {
            if (ticket.status().isOpen()) {
                open++;
            }
            if (!openOnly || ticket.status().isOpen()) {
                shown.add(ticket);
            }
        }
        view.getTable().getItems().setAll(shown);
        view.getTable().sort();
        view.getCountLabel().setText(open + " open / " + allTickets.size() + " total");

        // keep the technician's selection (and typed notes) across refreshes
        if (selectedId != null) {
            for (TicketDTO ticket : view.getTable().getItems()) {
                if (selectedId.equals(ticket.id())) {
                    view.getTable().getSelectionModel().select(ticket);
                    break;
                }
            }
        }
    }

    private void showSelected(TicketDTO ticket) {
        boolean none = ticket == null;
        if (none) {
            view.getSelectedLabel().setText("Select a ticket");
            view.getSelectedInfo().setText("Pick a ticket from the table to work on it.");
        } else {
            view.getSelectedLabel().setText("#" + ticket.id() + " - " + ticket.subject());
            view.getSelectedInfo().setText(Formats.label(ticket.status()) + "  ·  " + Formats.label(ticket.priority())
                    + " priority  ·  from " + ticket.createdByName());
        }
        boolean workable = !none && ticket.status().isOpen();
        view.getDetailsButton().setDisable(none);
        view.getStatusBox().setDisable(!workable);
        view.getUpdateStatusButton().setDisable(!workable);
        view.getResolutionArea().setDisable(!workable);
        view.getResolveButton().setDisable(!workable);

        // clear the form only when a *different* ticket is picked. A refresh briefly
        // deselects and reselects the same ticket, which must not wipe typed notes.
        if (!none && !ticket.id().equals(lastShownId)) {
            view.getStatusBox().setValue(null);
            view.getResolutionArea().clear();
            lastShownId = ticket.id();
        }
    }

    // ticket currently loaded in the work panel
    private Long lastShownId;

    private void updateStatus(TicketDTO ticket) {
        TicketStatus status = view.getStatusBox().getValue();
        if (status == null) {
            Alerts.warning("Please Check the Form", "Choose a new status (In Progress or Pending).");
            return;
        }
        if (status == ticket.status()) {
            Alerts.info("No Change", "Ticket #" + ticket.id() + " is already " + Formats.label(status) + ".");
            return;
        }
        Background.run(() -> api.updateStatus(ticket.id(), technicianId, status), updated -> {
            setStatus("Ticket #" + updated.id() + " is now " + Formats.label(updated.status()));
            view.getStatusBox().setValue(null);
            refresh();
        }, new Node[] {view.getUpdateStatusButton()});
    }

    private void resolve(TicketDTO ticket) {
        String notes = view.getResolutionArea().getText() == null ? "" : view.getResolutionArea().getText().trim();
        if (notes.isEmpty()) {
            Alerts.warning("Resolution Notes Required", "Describe what was done before marking the ticket resolved.");
            view.getResolutionArea().requestFocus();
            return;
        }
        if (notes.length() > 2000) {
            Alerts.warning("Please Check the Form", "Resolution notes must be 2000 characters or less.");
            return;
        }
        if (!Alerts.confirm("Resolve Ticket", "Mark ticket #" + ticket.id() + " as resolved? The user will be notified.")) {
            return;
        }
        Background.run(() -> api.resolveTicket(ticket.id(), technicianId, notes), resolved -> {
            view.getResolutionArea().clear();
            Alerts.info("Ticket Resolved", "Ticket #" + resolved.id() + " has been marked as resolved.");
            refresh();
        }, new Node[] {view.getResolveButton()});
    }

    private void openDetails(TicketDTO ticket) {
        if (TicketDetailsController.open(ticket)) {
            refresh();
        }
    }

    private void withSelected(java.util.function.Consumer<TicketDTO> action) {
        TicketDTO ticket = view.getTable().getSelectionModel().getSelectedItem();
        if (ticket == null) {
            Alerts.info("No Ticket Selected", "Select a ticket in the table first.");
            return;
        }
        action.accept(ticket);
    }

    @Override
    public void dispose() {
        autoRefresh.cancel();
        super.dispose();
    }
}
