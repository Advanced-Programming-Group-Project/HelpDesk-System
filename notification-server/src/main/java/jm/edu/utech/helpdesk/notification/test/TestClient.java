package jm.edu.utech.helpdesk.notification.test;
 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
 
import jm.edu.utech.helpdesk.notification.model.Notification;
import jm.edu.utech.helpdesk.notification.model.Request;
import jm.edu.utech.helpdesk.notification.model.Response;
 
/*
 * A console client for testing the Notification Server without the GUI.
 *
 * Option 1 - Listen: connect as a user and print every notification received.
 * Option 2 - Send:   type notifications in and send them to the server
 *                    (this is what the Spring Boot backend does).
 */
public class TestClient {
 
    public static void main(String[] args) {
 
        Scanner input = new Scanner(System.in);
 
        System.out.println("1 = Listen as a user");
        System.out.println("2 = Send notifications");
        System.out.println("Enter option: ");
        int option = input.nextInt();
        input.nextLine();
 
        try {
            Socket socket = new Socket("localhost", 5000);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream serverInput = new ObjectInputStream(socket.getInputStream());
 
            if (option == 1) {
                System.out.println("Enter user id: ");
                long userId = input.nextInt();
                input.nextLine();
                System.out.println("Enter role (USER, TECHNICIAN or ADMIN): ");
                String role = input.nextLine();
 
                output.writeObject(new Request("REGISTER", userId, role, null));
                Response response = (Response) serverInput.readObject();
                System.out.println(response.getMessage() + " - waiting for notifications...");
 
                while (true) {
                    response = (Response) serverInput.readObject();
                    System.out.println(">>> " + response.getNotification());
                }
            }
            else {
                boolean sending = true;
 
                while (sending) {
                    System.out.println("Type (TICKET_ASSIGNED, TICKET_RESOLVED, NEW_COMMENT, URGENT_TICKET) or EXIT: ");
                    String type = input.nextLine();
 
                    if (type.equals("EXIT")) {
                        sending = false;
                    }
                    else {
                        System.out.println("Ticket id: ");
                        long ticketId = input.nextInt();
                        input.nextLine();
                        System.out.println("Send to user id (0 for none): ");
                        long userId = input.nextInt();
                        input.nextLine();
                        System.out.println("Send to role - USER, TECHNICIAN or ADMIN (leave blank for none): ");
                        String role = input.nextLine();
                        System.out.println("Message: ");
                        String message = input.nextLine();
 
                        Notification notification = new Notification(type, ticketId, message);
                        if (userId != 0) {
                            notification.addUser(userId);
                        }
                        if (!role.isBlank()) {
                            notification.addRole(role);
                        }
 
                        output.writeObject(new Request("SEND_NOTIFICATION", 0, null, notification));
                        Response response = (Response) serverInput.readObject();
                        System.out.println("Server: " + response.getMessage());
                    }
                }
                output.writeObject(new Request("DISCONNECT", 0, null, null));
            }
            socket.close();
        }
        catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
