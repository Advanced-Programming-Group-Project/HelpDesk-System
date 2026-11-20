package jm.edu.utech.helpdesk.notification;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import jm.edu.utech.helpdesk.model.Comment;
import jm.edu.utech.helpdesk.model.Ticket;
import jm.edu.utech.helpdesk.notification.model.Notification;
import jm.edu.utech.helpdesk.service.NotificationService;

/*
 * Member 2's NotificationService, implemented with the TCP Notification Server.
 *
 * @Component tells Spring to create this object and inject it into
 * TicketService wherever a NotificationService is needed (Lecture 04).
 *
 * Each notification is sent by a SendNotificationTask on a virtual thread
 * (Lab 10, Part 2), so a REST request never waits for the Notification Server.
 */
@Component
public class SocketNotificationService implements NotificationService {

    private static final Logger logger = LogManager.getLogger(SocketNotificationService.class);

    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /*
     * Ticket Assigned -> ticket owner and the assigned technician.
     */
    @Override
    public void ticketAssigned(Ticket ticket) {
        Notification notification = new Notification("TICKET_ASSIGNED", ticket.getId(),
                "Ticket #" + ticket.getId() + " '" + ticket.getSubject() + "' has been assigned to a technician");
        addOwner(notification, ticket);
        addTechnician(notification, ticket);
        send(notification);
    }

    /*
     * Ticket Resolved -> ticket owner.
     */
    @Override
    public void ticketResolved(Ticket ticket) {
        Notification notification = new Notification("TICKET_RESOLVED", ticket.getId(),
                "Ticket #" + ticket.getId() + " '" + ticket.getSubject() + "' has been resolved");
        addOwner(notification, ticket);
        send(notification);
    }

    /*
     * New Comment Added -> ticket owner and the assigned technician.
     */
    @Override
    public void commentAdded(Ticket ticket, Comment comment) {
        Notification notification = new Notification("NEW_COMMENT", ticket.getId(),
                comment.getAuthor().getUsername() + " commented on Ticket #" + ticket.getId()
                + ": " + comment.getText());
        addOwner(notification, ticket);
        addTechnician(notification, ticket);
        send(notification);
    }

    /*
     * Urgent Ticket Created -> every ADMIN and TECHNICIAN who is online.
     */
    @Override
    public void urgentTicketCreated(Ticket ticket) {
        Notification notification = new Notification("URGENT_TICKET", ticket.getId(),
                "Urgent ticket #" + ticket.getId() + ": " + ticket.getSubject());
        notification.addRole("ADMIN");
        notification.addRole("TECHNICIAN");
        send(notification);
    }

    /*
     * Adds the user who created the ticket.
     */
    private void addOwner(Notification notification, Ticket ticket) {
        if (ticket.getCreatedBy() != null) {
            notification.addUser(ticket.getCreatedBy().getId());
        }
    }

    /*
     * Adds the technician's user account, if the ticket has been assigned.
     */
    private void addTechnician(Notification notification, Ticket ticket) {
        if (ticket.getAssignedTechnician() != null) {
            notification.addUser(ticket.getAssignedTechnician().getUser().getId());
        }
    }

    /*
     * Sends on a background thread and returns immediately.
     */
    private void send(Notification notification) {
        logger.info("Sending notification: " + notification);
        executor.submit(new SendNotificationTask(notification));
    }
}
