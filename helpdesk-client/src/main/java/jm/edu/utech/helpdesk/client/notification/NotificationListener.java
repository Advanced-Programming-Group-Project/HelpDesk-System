package jm.edu.utech.helpdesk.client.notification;
// Member 1 (Front-End)

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javafx.application.Platform;

import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.AppConfig;

// connects to the TCP notification server (Member 4) on a background thread
// and passes each line it receives to the GUI.
//
// protocol (plain text, one message per line, UTF-8):
//   client -> server on connect:  REGISTER <userId> <role> <username>
//   server -> client:             any line, shown as-is in the notifications panel
// member 4 input: change register()/the line format here if the server uses something else.
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
        int port = AppConfig.getInt("notification.port", 5050);

        while (running) {
            try (Socket s = new Socket()) {
                socket = s;
                s.connect(new InetSocketAddress(host, port), 5000);

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
                register(out);
                fx(() -> onConnectionChange.accept(true));

                String line;
                while (running && (line = in.readLine()) != null) {
                    String message = line.trim();
                    if (!message.isEmpty()) {
                        fx(() -> onMessage.accept(message));
                    }
                }
            } catch (IOException e) {
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

    private void register(PrintWriter out) {
        out.println("REGISTER " + user.id() + " " + user.role() + " " + user.username());
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
