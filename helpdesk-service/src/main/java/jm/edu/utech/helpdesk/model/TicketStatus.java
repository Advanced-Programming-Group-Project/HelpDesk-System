package jm.edu.utech.helpdesk.model;
//created by rickayla mcneil 2111687
// ticket statuses and which changes are allowed
public enum TicketStatus {
    NEW,
    ASSIGNED,
    IN_PROGRESS,
    PENDING,
    RESOLVED,
    CLOSED,
    REOPENED;

    public boolean canChangeTo(TicketStatus next) {
        switch (this) {
            case NEW:
                return next == ASSIGNED || next == CLOSED;
            case ASSIGNED:
                return next == IN_PROGRESS || next == PENDING || next == RESOLVED || next == CLOSED;
            case IN_PROGRESS:
                return next == PENDING || next == RESOLVED || next == CLOSED;
            case PENDING:
                return next == IN_PROGRESS || next == RESOLVED || next == CLOSED;
            case RESOLVED:
                return next == CLOSED || next == REOPENED;
            case CLOSED:
                return next == REOPENED;
            case REOPENED:
                return next == ASSIGNED || next == IN_PROGRESS || next == CLOSED;
            default:
                return false;
        }
    }

    // open = not resolved or closed
    public boolean isOpen() {
        return this != RESOLVED && this != CLOSED;
    }
}
