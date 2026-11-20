package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

// ticket statuses (matches the backend enum)
public enum TicketStatus {
    NEW,
    ASSIGNED,
    IN_PROGRESS,
    PENDING,
    RESOLVED,
    CLOSED,
    REOPENED;

    // open = not resolved or closed
    public boolean isOpen() {
        return this != RESOLVED && this != CLOSED;
    }
}
