package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDateTime;

// error body the server sends when a request fails
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message) {
}
