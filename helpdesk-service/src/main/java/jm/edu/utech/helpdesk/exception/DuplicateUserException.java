package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// username or email already exists
public class DuplicateUserException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateUserException(String message) {
        super(message);
    }
}
