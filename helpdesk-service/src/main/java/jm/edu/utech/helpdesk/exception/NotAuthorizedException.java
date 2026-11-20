package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// role not allowed to do this
public class NotAuthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotAuthorizedException(String message) {
        super(message);
    }
}
