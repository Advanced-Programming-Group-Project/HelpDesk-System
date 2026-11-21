package jm.edu.utech.helpdesk.client.notification;
// Member 1 (Front-End) - connection code updated by Member 4 to match the Notification Server

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

import javafx.application.Platform;

import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.AppConfig;
import jm.edu.utech.helpdesk.notification.model.Notification;
import jm.edu.utech.helpdesk.notification.model.Request;
import jm.edu.utech.helpdesk.notification.model.Response;

// connects to the TCP notification server (Member 4) on a background thread
// and passes each notification it receives to the GUI.
//
// protocol (Serializable objects, as in Lab 09):
//   client -> server on connect:  Request("REGISTER", userId, role, null)
//   server -> client:             Response - first the registration reply,
//                                 then one Response per notification
public class NotificationListener {

    private static final int RETRY_SECONDS = 10;

    private final UserDTO user;
    private final Consumer<String> onMessage;
    private final Consumer<Boolean> onConnectionChange;

    private volatile boolean running;
    private volatile Socket socket;
    private Thread thread;

    // callbacks always run on the JavaFX Application Thread
    public NotificationListener(UserDTO user, Consumer<String> onMessage, Consumer<Boolean> onConnectionChange) {
        this.user = user;
        this.onMessage = onMessage;
        this.onConnectionChange = onConnectionChange;
    }

    public void start() {
        running = true;
        thread = new Thread(this::listenLoop, "notification-listener");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        closeSocket();
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void listenLoop() {
        String host = AppConfig.get("notification.host", "localhost");
        int port = AppConfig.getInt("notification.port", 5000);

        while (running) {
            try (Socket s = new Socket()) {
                socket = s;
                s.connect(new InetSocketAddress(host, port), 5000);

                // output stream first, then input stream (same order as the server)
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                if (register(out, in)) {
                    fx(() -> onConnectionChange.accept(true));

                    while (running) {
                        Response response = (Response) in.readObject();
                        Notification notification = response.getNotification();
                        if (notification != null) {
                            String message = notification.toString();
                            fx(() -> onMessage.accept(message));
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // server not running or connection dropped - try again shortly
            } finally {
                socket = null;
            }

            if (running) {
                fx(() -> onConnectionChange.accept(false));
                sleepBeforeRetry();
            }
        }
    }

    // sends REGISTER with the logged-in user's id and role; returns true if the server accepted it
    private boolean register(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        out.writeObject(new Request("REGISTER", user.id(), user.role().toString(), null));
        Response reply = (Response) in.readObject();
        return reply.isSuccess();
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_SECONDS * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeSocket() {
        Socket s = socket;
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignored) {
                // closing anyway
            }
        }
    }

    private void fx(Runnable action) {
        if (running) {
            Platform.runLater(action);
        }
    }
}
