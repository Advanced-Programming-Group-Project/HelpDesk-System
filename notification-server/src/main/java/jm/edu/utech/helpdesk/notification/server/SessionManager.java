package jm.edu.utech.helpdesk.notification.server;
 
import java.util.ArrayList;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
public class SessionManager {
 
    private static final Logger logger = LogManager.getLogger(SessionManager.class);
 
    private ArrayList<ClientHandler> clients = new ArrayList<>();
 
    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        logger.info("User #" + client.getUserId() + " (" + client.getRole() + ") connected. Online: " + clients.size());
    }
 
    
     // Rebuilds the list without this client.
     
    public synchronized void removeClient(ClientHandler client) {
        ArrayList<ClientHandler> remaining = new ArrayList<>();
        boolean found = false;
 
        for (ClientHandler c : clients) {
            if (c == client) {
                found = true;
            }
            else {
                remaining.add(c);
            }
        }
        clients = remaining;
 
        if (found) {
            logger.info("User #" + client.getUserId() + " disconnected. Online: " + clients.size());
        }
    }
 
    /*
     * Returns a copy of the list, so it can be looped through
     * while other threads add or remove clients.
     */
    public synchronized ArrayList<ClientHandler> getClients() {
        ArrayList<ClientHandler> copy = new ArrayList<>();
        for (ClientHandler c : clients) {
            copy.add(c);
        }
        return copy;
    }
}
