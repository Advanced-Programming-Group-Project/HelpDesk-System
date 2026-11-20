package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

// holds the logged-in user for the life of the app
public class Session {

    private static UserDTO currentUser;
    private static Long technicianId;   // only set when a technician logs in

    private Session() {
    }

    public static void start(UserDTO user, Long techId) {
        currentUser = user;
        technicianId = techId;
    }

    public static void clear() {
        currentUser = null;
        technicianId = null;
    }

    public static UserDTO getUser() {
        return currentUser;
    }

    public static void setUser(UserDTO user) {
        currentUser = user;
    }

    public static Long getUserId() {
        return currentUser == null ? null : currentUser.id();
    }

    public static Long getTechnicianId() {
        return technicianId;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
