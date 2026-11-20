package jm.edu.utech.helpdesk.notification.server;
 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
import jm.edu.utech.helpdesk.notification.exception.InvalidRequestException;
import jm.edu.utech.helpdesk.notification.model.Notification;
import jm.edu.utech.helpdesk.notification.model.Request;
import jm.edu.utech.helpdesk.notification.model.Response;
 
public class ClientHandler extends Thread {
 
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
 
    private Socket socket;
    private SessionManager sessionManager;
    private ExecutorService executor;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private long userId;
    private String role;
 
    public ClientHandler(Socket socket, SessionManager sessionManager, ExecutorService executor) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.executor = executor;
    }
 
    @Override
    public void run() {
        try {

            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
 
            boolean connected = true;
 
            while (connected) {

                Request request = (Request) input.readObject();
 
                try {
                    connected = handleRequest(request);
                }
                catch (InvalidRequestException ex) {
                    logger.warn("Invalid request: " + ex.getMessage());
                    sendResponse(new Response(false, ex.getMessage(), null));
                }
            }
        }
        catch (IOException ex) {
            logger.info("Connection closed for User #" + userId);
        }
        catch (ClassNotFoundException ex) {
            logger.error("Unknown object received from client: " + ex.getMessage());
        }
        finally {

            sessionManager.removeClient(this);
            try {
                socket.close();
            }
            catch (IOException ex) {
                logger.error("Error closing socket: " + ex.getMessage());
            }
        }
    }
 
    private boolean handleRequest(Request request) throws InvalidRequestException {
 
        if (request.getCommand() == null) {
            throw new InvalidRequestException("Request has no command");
        }
 
        if (request.getCommand().equals("REGISTER")) {
            if (request.getRole() == null) {
                throw new InvalidRequestException("REGISTER needs a role");
            }
            userId = request.getUserId();
            role = request.getRole();
            sessionManager.addClient(this);
            sendResponse(new Response(true, "Registered", null));
            return true;
        }
        else if (request.getCommand().equals("SEND_NOTIFICATION")) {
            Notification notification = request.getNotification();
            if (notification == null) {
                throw new InvalidRequestException("SEND_NOTIFICATION needs a notification");
            }
            logger.info("Notification received: " + notification);
 
            executor.submit(new BroadcastTask(notification, sessionManager));
 
            sendResponse(new Response(true, "Notification accepted", null));
            return true;
        }
        else if (request.getCommand().equals("DISCONNECT")) {
            return false;
        }
        else {
            throw new InvalidRequestException("Unknown command: " + request.getCommand());
        }
    }
 

    public synchronized boolean sendResponse(Response response) {
        try {
            output.writeObject(response);
            return true;
        }
        catch (IOException ex) {
            return false;
        }
    }
 
    public long getUserId() {
        return userId;
    }
 
    public String getRole() {
        return role;
    }
}
