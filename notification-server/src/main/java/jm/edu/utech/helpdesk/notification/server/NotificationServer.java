package jm.edu.utech.helpdesk.notification.server;
 
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 

public class NotificationServer {
 
    private static final Logger logger = LogManager.getLogger(NotificationServer.class);
 
    public static void main(String[] args) {
 
        SessionManager sessionManager = new SessionManager();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
 
        try {
            ServerSocket server = new ServerSocket(5000);
 
            logger.info("Notification Server Started on port 5000");
            logger.info("Waiting For Clients...");
 
            while (true) {
                Socket socket = server.accept();
 
                logger.info("Client Connected");
 
                ClientHandler handler = new ClientHandler(socket, sessionManager, executor);
                handler.start();
            }
        }
        catch (Exception ex) {
            logger.fatal("Notification Server stopped: " + ex.getMessage());
        }
    }
}
