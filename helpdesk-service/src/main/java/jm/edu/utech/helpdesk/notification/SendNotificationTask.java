package jm.edu.utech.helpdesk.notification;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jm.edu.utech.helpdesk.notification.model.Notification;
import jm.edu.utech.helpdesk.notification.model.Request;
import jm.edu.utech.helpdesk.notification.model.Response;

/*
 * Sends one notification to the Notification Server.
 *
 * Runs on a background (virtual) thread, so the REST request
 * that caused the notification does not have to wait.
 */
public class SendNotificationTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(SendNotificationTask.class);

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private Notification notification;

    public SendNotificationTask(Notification notification) {
        this.notification = notification;
    }

    /*
     * Connects to the server, sends the notification, receives a response.
     */
    @Override
    public void run() {
        try {
            Socket socket = new Socket(HOST, PORT);

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            output.writeObject(new Request("SEND_NOTIFICATION", 0, null, notification));
            Response response = (Response) input.readObject();

            output.writeObject(new Request("DISCONNECT", 0, null, null));
            socket.close();

            if (response.isSuccess()) {
                logger.info("Notification sent: " + notification);
            }
            else {
                logger.error("Notification failure: " + response.getMessage());
            }
        }
        catch (Exception ex) {
            logger.error("Notification failure: could not reach the Notification Server - " + ex.getMessage());
        }
    }
}
