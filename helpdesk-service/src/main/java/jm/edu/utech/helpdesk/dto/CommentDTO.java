package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;
import jm.edu.utech.helpdesk.model.Role;

// comment info sent to the client
public record CommentDTO(
        Long id,
        Long ticketId,
        Long authorId,
        String authorName,
        Role authorRole,
        String text,
        LocalDateTime createdAt) {
}
