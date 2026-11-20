package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
import jm.edu.utech.helpdesk.model.Comment;
import jm.edu.utech.helpdesk.model.Ticket;

// ticket notifications
// member 4: implemented by notification.SocketNotificationService (@Component)
public interface NotificationService {

    public abstract void ticketAssigned(Ticket ticket);

    public abstract void ticketResolved(Ticket ticket);

    public abstract void commentAdded(Ticket ticket, Comment comment);

    public abstract void urgentTicketCreated(Ticket ticket);
}
