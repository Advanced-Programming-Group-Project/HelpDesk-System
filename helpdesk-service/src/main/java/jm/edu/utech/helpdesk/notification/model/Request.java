package jm.edu.utech.helpdesk.notification.model;
 
import java.io.Serializable;
 

 //Represents a request received from a client.

public class Request implements Serializable {
 
    private String command;
    private long userId;
    private String role;
    private Notification notification;
 
    public Request(String command, long userId, String role, Notification notification) {
        this.command = command;
        this.userId = userId;
        this.role = role;
        this.notification = notification;
    }
 
    public String getCommand() {
        return command;
    }
 
    public long getUserId() {
        return userId;
    }
 
    public String getRole() {
        return role;
    }
 
    public Notification getNotification() {
        return notification;
    }
}
