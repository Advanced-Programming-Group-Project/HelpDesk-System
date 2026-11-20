package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// status change not allowed
public class InvalidStatusChangeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidStatusChangeException(String message) {
        super(message);
    }
}
