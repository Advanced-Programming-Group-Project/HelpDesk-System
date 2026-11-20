package jm.edu.utech.helpdesk.notification.model;
 
import java.io.Serializable;
import java.util.ArrayList;
 

public class Notification implements Serializable {
 
    private String type;
    private long ticketId;
    private String message;
    private ArrayList<Long> userIds;
    private ArrayList<String> roles;
 
    public Notification(String type, long ticketId, String message) {
        this.type = type;
        this.ticketId = ticketId;
        this.message = message;
        this.userIds = new ArrayList<>();
        this.roles = new ArrayList<>();
    }
 
    
     //Send this notification to one user.
     
    public void addUser(long userId) {
        userIds.add(userId);
    }
 

     // Send this notification to everyone with this role.

    public void addRole(String role) {
        roles.add(role);
    }
 
// Checks if a connected user should receive this notification.
    public boolean isFor(long userId, String role) {
        for (Long id : userIds) {
            if (id == userId) {
                return true;
            }
        }
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }
 
    
   //  A readable title for the notification pop-up.
     
    public String getTitle() {
        if (type.equals("TICKET_ASSIGNED")) {
            return "Ticket Assigned";
        }
        else if (type.equals("TICKET_RESOLVED")) {
            return "Ticket Resolved";
        }
        else if (type.equals("NEW_COMMENT")) {
            return "New Comment Added";
        }
        else if (type.equals("URGENT_TICKET")) {
            return "Urgent Ticket Created";
        }
        return "Notification";
    }
 
    public String getType() {
        return type;
    }
 
    public long getTicketId() {
        return ticketId;
    }
 
    public String getMessage() {
        return message;
    }
 
    public ArrayList<Long> getUserIds() {
        return userIds;
    }
 
    public ArrayList<String> getRoles() {
        return roles;
    }
 
    @Override
    public String toString() {
        return getTitle() + " (Ticket #" + ticketId + "): " + message;
    }
}
