package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;

// confirmation message
public record MessageResponse(
        String message,
        LocalDateTime timestamp) {
}
