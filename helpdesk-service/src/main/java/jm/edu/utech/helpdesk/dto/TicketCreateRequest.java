package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import jm.edu.utech.helpdesk.model.Priority;

// create a ticket
public record TicketCreateRequest(
        Long userId,
        String subject,
        String description,
        Priority priority) {
}
