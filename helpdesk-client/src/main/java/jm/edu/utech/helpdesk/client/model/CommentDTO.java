package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDateTime;

// comment received from the server
public record CommentDTO(
        Long id,
        Long ticketId,
        Long authorId,
        String authorName,
        Role authorRole,
        String text,
        LocalDateTime createdAt) {
}
