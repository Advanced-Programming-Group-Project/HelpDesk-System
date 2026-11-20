package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// ticket can't be assigned
public class TicketAssignmentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TicketAssignmentException(String message) {
        super(message);
    }
}
