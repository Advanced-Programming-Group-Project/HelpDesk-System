package jm.edu.utech.helpdesk.notification.exception;
 
/*
 * Thrown when a client sends a request that is missing information
 * or has a command the server does not recognise.
 */
public class InvalidRequestException extends Exception {
 
    public InvalidRequestException(String message) {
        super(message);
    }
}
