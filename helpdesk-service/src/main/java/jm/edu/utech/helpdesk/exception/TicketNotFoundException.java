package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// ticket not found
public class TicketNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(String message) {
        super(message);
    }
}
