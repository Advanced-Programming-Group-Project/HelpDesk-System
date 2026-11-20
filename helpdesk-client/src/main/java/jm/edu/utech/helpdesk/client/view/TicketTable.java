package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import java.time.LocalDateTime;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.util.Formats;

// builds the TableView of tickets used on every dashboard
public final class TicketTable {

    private TicketTable() {
    }

    public static TableView<TicketDTO> create(boolean showRequester, boolean showTechnician) {
        TableView<TicketDTO> table = new TableView<>();
        table.setPlaceholder(new Label("No tickets to show"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<TicketDTO, Long> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().id()));
        id.setMinWidth(50);
        id.setPrefWidth(55);
        id.setMaxWidth(80);

        TableColumn<TicketDTO, String> subject = text("Subject", TicketDTO::subject);
        subject.setMinWidth(140);
        subject.setPrefWidth(240);

        TableColumn<TicketDTO, Enum<?>> priority = badge("Priority", TicketDTO::priority, "priority-");
        priority.setMinWidth(80);
        priority.setPrefWidth(90);
        // sort by severity, not alphabetically
        priority.setComparator((a, b) -> Integer.compare(a == null ? -1 : a.ordinal(), b == null ? -1 : b.ordinal()));

        TableColumn<TicketDTO, Enum<?>> status = badge("Status", TicketDTO::status, "status-");
        status.setMinWidth(95);
        status.setPrefWidth(110);

        TableColumn<TicketDTO, LocalDateTime> created = date("Created", TicketDTO::creationDate);
        TableColumn<TicketDTO, LocalDateTime> updated = date("Last Updated", TicketDTO::lastUpdated);

        table.getColumns().add(id);
        table.getColumns().add(subject);
        table.getColumns().add(priority);
        table.getColumns().add(status);
        if (showRequester) {
            table.getColumns().add(text("Submitted By", TicketDTO::createdByName));
        }
        if (showTechnician) {
            table.getColumns().add(text("Technician", t -> t.assignedTechnicianName() == null
                    ? "Unassigned" : t.assignedTechnicianName()));
        }
        table.getColumns().add(created);
        table.getColumns().add(updated);

        // newest first by default
        id.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(id);
        return table;
    }

    private static TableColumn<TicketDTO, String> text(String title, Function<TicketDTO, String> getter) {
        TableColumn<TicketDTO, String> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        return column;
    }

    private static TableColumn<TicketDTO, LocalDateTime> date(String title, Function<TicketDTO, LocalDateTime> getter) {
        TableColumn<TicketDTO, LocalDateTime> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(getter.apply(c.getValue())));
        column.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : Formats.dateTime(item));
            }
        });
        column.setPrefWidth(150);
        return column;
    }

    // coloured pill for status / priority
    private static TableColumn<TicketDTO, Enum<?>> badge(String title, Function<TicketDTO, Enum<?>> getter,
                                                         String stylePrefix) {
        TableColumn<TicketDTO, Enum<?>> column = new TableColumn<>(title);
        column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(getter.apply(c.getValue())));
        column.setCellFactory(c -> new TableCell<>() {
            private final Label label = new Label();

            @Override
            protected void updateItem(Enum<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                label.setText(Formats.label(item));
                label.getStyleClass().setAll("label", "badge", stylePrefix + item.name().toLowerCase());
                setGraphic(label);
            }
        });
        return column;
    }
}
