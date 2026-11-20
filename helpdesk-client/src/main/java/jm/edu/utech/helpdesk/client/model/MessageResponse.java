package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDateTime;

// confirmation message from the server
public record MessageResponse(
        String message,
        LocalDateTime timestamp) {
}
