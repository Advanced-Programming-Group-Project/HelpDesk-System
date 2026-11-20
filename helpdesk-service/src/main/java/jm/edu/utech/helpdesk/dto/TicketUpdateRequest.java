package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import jm.edu.utech.helpdesk.model.Priority;

// owner updates ticket details
public record TicketUpdateRequest(
        Long userId,
        String subject,
        String description,
        Priority priority) {
}
