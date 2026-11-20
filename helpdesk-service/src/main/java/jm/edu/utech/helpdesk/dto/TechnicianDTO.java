package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// technician info sent to the client
public record TechnicianDTO(
        Long id,
        Long userId,
        String username,
        String fullName,
        String email,
        String specialization,
        boolean available,
        int activeTickets) {
}
