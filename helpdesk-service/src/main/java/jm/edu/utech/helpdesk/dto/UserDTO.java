package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;
import jm.edu.utech.helpdesk.model.Role;

// user info sent to the client
public record UserDTO(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt) {
}
