package jm.edu.utech.helpdesk.notification.model;
 
import java.io.Serializable;
 
/*
 * Sent from the server to a client.
 * Either a reply to a request, or a notification being delivered.
 */
public class Response implements Serializable {
 
    private boolean success;
    private String message;
    private Notification notification;
 
    public Response(boolean success, String message, Notification notification) {
        this.success = success;
        this.message = message;
        this.notification = notification;
    }
 
    public boolean isSuccess() {
        return success;
    }
 
    public String getMessage() {
        return message;
    }
 
    public Notification getNotification() {
        return notification;
    }
}
