package jm.edu.utech.helpdesk.notification.server;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
import jm.edu.utech.helpdesk.notification.model.Notification;
import jm.edu.utech.helpdesk.notification.model.Response;
 
/*
 * Sends one notification to every connected client it is meant for.
 *
 * Runs on a background (virtual) thread, so the ClientHandler that
 * received the notification does not have to wait for it to be sent.
 */
public class BroadcastTask implements Runnable {
 
    private static final Logger logger = LogManager.getLogger(BroadcastTask.class);
 
    private Notification notification;
    private SessionManager sessionManager;
 
    public BroadcastTask(Notification notification, SessionManager sessionManager) {
        this.notification = notification;
        this.sessionManager = sessionManager;
    }
 
    @Override
    public void run() {
        int sent = 0;
 
        for (ClientHandler client : sessionManager.getClients()) {
 
            if (notification.isFor(client.getUserId(), client.getRole())) {
 
                Response response = new Response(true, "New notification", notification);
 
                if (client.sendResponse(response)) {
                    sent++;
                }
                else {
                    logger.error("Notification failure: could not send " + notification.getType()
                            + " to User #" + client.getUserId());
                }
            }
        }
        logger.info("Broadcast " + notification.getType() + " for Ticket #"
                + notification.getTicketId() + " to " + sent + " client(s)");
    }
}
