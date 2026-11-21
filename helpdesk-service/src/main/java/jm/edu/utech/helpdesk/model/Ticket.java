package jm.edu.utech.helpdesk.model;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

// a support ticket
// member 3: JPA entity mapped to the tickets table
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // ticket ID is auto-generated
    @Column(name = "ticket_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    // Many-to-One: many tickets belong to one user (the User side is One-to-Many)
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Many-to-One: many tickets can be assigned to one technician (null until assigned)
    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician assignedTechnician;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;   // used for average resolution time

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // One-to-Many: one ticket has many comments
    @OneToMany(mappedBy = "ticket")
    private List<Comment> comments = new ArrayList<>();

    public Ticket() {
    }

    public Ticket(String subject, String description, Priority priority, User createdBy) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.createdBy = createdBy;
        this.status = TicketStatus.NEW;
        this.creationDate = LocalDateTime.now();
        this.lastUpdated = this.creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Technician getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(Technician assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public LocalDateTime getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDateTime closedDate) {
        this.closedDate = closedDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // comments on this ticket (only loaded while the EntityManager is open)
    public List<Comment> getComments() {
        return comments;
    }
}
