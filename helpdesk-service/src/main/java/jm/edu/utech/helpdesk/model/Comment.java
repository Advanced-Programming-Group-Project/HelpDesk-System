package jm.edu.utech.helpdesk.model;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;

// a comment on a ticket
// member 3 input: JPA annotations
public class Comment {

    private Long id;
    private Ticket ticket;
    private User author;
    private String text;
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(Ticket ticket, User author, String text) {
        this.ticket = ticket;
        this.author = author;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
