package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// user not found
public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }
}
