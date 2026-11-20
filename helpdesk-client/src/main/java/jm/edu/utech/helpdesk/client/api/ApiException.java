package jm.edu.utech.helpdesk.client.api;
// Member 1 (Front-End)

// thrown when a REST call fails; carries the HTTP status so the GUI can pick the right alert
public class ApiException extends RuntimeException {

    // status used when the server could not be reached at all
    public static final int NO_CONNECTION = 0;

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean isConnectionError() {
        return status == NO_CONNECTION;
    }

    // a short title for the alert window
    public String getTitle() {
        return switch (status) {
            case NO_CONNECTION -> "Unable to Connect to Server";
            case 400 -> "Invalid Request";
            case 401 -> "Invalid Login Credentials";
            case 403 -> "Not Allowed";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            default -> "Server Error";
        };
    }
}
