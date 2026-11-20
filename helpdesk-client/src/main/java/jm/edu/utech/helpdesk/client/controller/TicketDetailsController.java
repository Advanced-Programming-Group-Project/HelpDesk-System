package jm.edu.utech.helpdesk.client.controller;
// Member 1 (Front-End)

import java.util.List;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import jm.edu.utech.helpdesk.client.api.HelpDeskApi;
import jm.edu.utech.helpdesk.client.model.CommentDTO;
import jm.edu.utech.helpdesk.client.model.Session;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;
import jm.edu.utech.helpdesk.client.util.Alerts;
import jm.edu.utech.helpdesk.client.util.Background;
import jm.edu.utech.helpdesk.client.util.FormValidator;
import jm.edu.utech.helpdesk.client.view.TicketDetailsView;

// handles the ticket details window (view ticket + comments, add a comment)
public class TicketDetailsController {

    private final TicketDetailsView view;
    private final Long ticketId;
    private final HelpDeskApi api = HelpDeskApi.get();
    private boolean changed;

    private TicketDetailsController(TicketDetailsView view, Stage stage, Long ticketId) {
        this.view = view;
        this.ticketId = ticketId;
        view.getAddCommentButton().setOnAction(event -> addComment());
        view.getCloseButton().setOnAction(event -> stage.close());
    }

    // opens the window; returns true if a comment was added (so the caller can refresh)
    public static boolean open(TicketDTO ticket) {
        TicketDetailsView view = new TicketDetailsView();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ticket #" + ticket.id());
        Scene scene = new Scene(view.getRoot());
        scene.getStylesheets().add(TicketDetailsController.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);

        TicketDetailsController controller = new TicketDetailsController(view, stage, ticket.id());
        view.showTicket(ticket);
        controller.load();
        stage.showAndWait();
        return controller.changed;
    }

    // fetch the latest ticket + comments in the background
    private void load() {
        Background.run(() -> new Details(api.getTicket(ticketId), api.getComments(ticketId)), details -> {
            view.showTicket(details.ticket());
            view.showComments(details.comments());
            boolean closed = details.ticket().status() == TicketStatus.CLOSED;
            view.getAddCommentButton().setDisable(closed);
            view.getNewCommentArea().setDisable(closed);
            if (closed) {
                view.getNewCommentArea().setPromptText("Comments cannot be added to a closed ticket.");
            }
        });
    }

    private void addComment() {
        FormValidator validator = new FormValidator().required(view.getNewCommentArea(), "Comment", 1000);
        if (!validator.isValid()) {
            validator.showErrors();
            return;
        }
        String text = view.getNewCommentArea().getText().trim();
        Long authorId = Session.getUserId();

        Background.run(() -> api.addComment(ticketId, authorId, text), comment -> {
            changed = true;
            view.getNewCommentArea().clear();
            load();
        }, new Node[] {view.getAddCommentButton()});
    }

    private record Details(TicketDTO ticket, List<CommentDTO> comments) {
    }
}
