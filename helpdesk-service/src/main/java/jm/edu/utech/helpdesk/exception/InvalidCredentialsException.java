package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// login failed
public class InvalidCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
