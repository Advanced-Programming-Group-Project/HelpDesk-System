package jm.edu.utech.helpdesk.client.util;
// Member 1 (Front-End)

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javafx.concurrent.Task;
import javafx.scene.Node;

// runs REST calls on a background thread so the GUI never freezes,
// then hands the result back on the JavaFX Application Thread.
public final class Background {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "helpdesk-worker");
        thread.setDaemon(true);   // don't keep the app alive after the window closes
        return thread;
    });

    private Background() {
    }

    // run work in the background; onSuccess runs on the FX thread; errors show an alert
    public static <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess) {
        return run(work, onSuccess, Alerts::error, null);
    }

    // same, but disables the given controls (e.g. the button pressed) while the call is running
    public static <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Node... busy) {
        return run(work, onSuccess, Alerts::error, busy);
    }

    public static <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError,
                                  Node[] busy) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };

        setDisabled(busy, true);

        task.setOnSucceeded(event -> {
            setDisabled(busy, false);
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });
        task.setOnFailed(event -> {
            setDisabled(busy, false);
            if (onError != null) {
                onError.accept(task.getException());
            }
        });

        POOL.submit(task);
        return task;
    }

    private static void setDisabled(Node[] nodes, boolean disabled) {
        if (nodes == null) {
            return;
        }
        for (Node node : nodes) {
            if (node != null) {
                node.setDisable(disabled);
            }
        }
    }

    public static void shutdown() {
        POOL.shutdownNow();
    }
}
