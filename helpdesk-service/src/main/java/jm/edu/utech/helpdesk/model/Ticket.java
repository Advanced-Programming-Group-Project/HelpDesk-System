package jm.edu.utech.helpdesk.model;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;

// a support ticket
// member 3 input: JPA annotations
public class Ticket {

    private Long id;
    private String subject;
    private String description;
    private LocalDateTime creationDate;
    private Priority priority;
    private TicketStatus status;
    private User createdBy;
    private Technician assignedTechnician;
    private String resolutionNotes;
    private LocalDateTime resolvedDate;   // used for average resolution time
    private LocalDateTime closedDate;
    private LocalDateTime lastUpdated;

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
}
