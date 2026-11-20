package jm.edu.utech.helpdesk.client.view;
// Member 1 (Front-End)

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import jm.edu.utech.helpdesk.client.model.CommentDTO;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.util.Formats;

// ticket details window: all ticket fields, the comment thread and an "add comment" box
public class TicketDetailsView {

    private final VBox root = new VBox(12);
    private final Label titleLabel = new Label();
    private final GridPane details = new GridPane();
    private final TextArea descriptionArea = new TextArea();
    private final TextArea resolutionArea = new TextArea();
    private final VBox commentsBox = new VBox(8);
    private final TextArea newCommentArea = new TextArea();
    private final Button addCommentButton = new Button("Add Comment");
    private final Button closeButton = new Button("Close");

    public TicketDetailsView() {
        titleLabel.getStyleClass().add("panel-title");
        titleLabel.setWrapText(true);

        details.setHgap(12);
        details.setVgap(6);
        details.getColumnConstraints().addAll(new ColumnConstraints(130), new ColumnConstraints());

        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        resolutionArea.setEditable(false);
        resolutionArea.setWrapText(true);
        resolutionArea.setPrefRowCount(2);

        ScrollPane commentScroll = new ScrollPane(commentsBox);
        commentScroll.setFitToWidth(true);
        commentScroll.setPrefViewportHeight(200);
        VBox.setVgrow(commentScroll, Priority.ALWAYS);

        newCommentArea.setPromptText("Write a comment (max 1000 characters)");
        newCommentArea.setWrapText(true);
        newCommentArea.setPrefRowCount(3);
        addCommentButton.getStyleClass().add("primary-button");
        closeButton.setCancelButton(true);
        HBox buttons = new HBox(8, closeButton, addCommentButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, details,
                section("Description"), descriptionArea,
                section("Resolution Notes"), resolutionArea,
                new Separator(),
                section("Comments"), commentScroll,
                newCommentArea, buttons);
        root.setPadding(new Insets(20));
        root.setPrefSize(620, 720);
    }

    public void showTicket(TicketDTO ticket) {
        titleLabel.setText("Ticket #" + ticket.id() + " - " + ticket.subject());
        details.getChildren().clear();
        int row = 0;
        details.addRow(row++, field("Status"), new Label(Formats.label(ticket.status())));
        details.addRow(row++, field("Priority"), new Label(Formats.label(ticket.priority())));
        details.addRow(row++, field("Submitted by"), new Label(Formats.orDash(ticket.createdByName())));
        details.addRow(row++, field("Technician"), new Label(ticket.assignedTechnicianName() == null
                ? "Unassigned" : ticket.assignedTechnicianName()));
        details.addRow(row++, field("Created"), new Label(Formats.dateTime(ticket.creationDate())));
        details.addRow(row++, field("Last updated"), new Label(Formats.dateTime(ticket.lastUpdated())));
        if (ticket.resolvedDate() != null) {
            details.addRow(row++, field("Resolved"), new Label(Formats.dateTime(ticket.resolvedDate())));
        }
        if (ticket.closedDate() != null) {
            details.addRow(row, field("Closed"), new Label(Formats.dateTime(ticket.closedDate())));
        }
        descriptionArea.setText(ticket.description());
        resolutionArea.setText(ticket.resolutionNotes() == null ? "Not resolved yet." : ticket.resolutionNotes());
    }

    public void showComments(java.util.List<CommentDTO> comments) {
        commentsBox.getChildren().clear();
        if (comments.isEmpty()) {
            Label none = new Label("No comments yet.");
            none.getStyleClass().add("hint");
            commentsBox.getChildren().add(none);
            return;
        }
        for (CommentDTO comment : comments) {
            Label author = new Label(comment.authorName() + "  ·  " + Formats.label(comment.authorRole()));
            author.getStyleClass().add("comment-author");
            Label time = new Label(Formats.dateTime(comment.createdAt()));
            time.getStyleClass().add("comment-time");
            Label text = new Label(comment.text());
            text.setWrapText(true);
            VBox box = new VBox(3, author, time, text);
            box.getStyleClass().add("comment-box");
            commentsBox.getChildren().add(box);
        }
    }

    private static Label section(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }

    private static Label field(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    public Parent getRoot() {
        return root;
    }

    public TextArea getNewCommentArea() {
        return newCommentArea;
    }

    public Button getAddCommentButton() {
        return addCommentButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }
}
