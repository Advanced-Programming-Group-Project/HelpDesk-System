package jm.edu.utech.helpdesk.exception;
//created by rickayla mcneil 2111687
// technician not found
public class TechnicianNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TechnicianNotFoundException(String message) {
        super(message);
    }
}
