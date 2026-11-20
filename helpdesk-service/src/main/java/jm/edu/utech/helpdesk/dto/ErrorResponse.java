package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;

// error sent back when a request fails
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message) {
}
