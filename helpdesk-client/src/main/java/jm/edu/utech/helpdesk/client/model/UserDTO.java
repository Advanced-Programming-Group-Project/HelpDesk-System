package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDateTime;

// user info received from the server
public record UserDTO(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt) {

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
