package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

// technician info received from the server
public record TechnicianDTO(
        Long id,
        Long userId,
        String username,
        String fullName,
        String email,
        String specialization,
        boolean available,
        int activeTickets) {

    @Override
    public String toString() {
        return fullName + " - " + specialization + " (" + activeTickets + " open)";
    }
}
