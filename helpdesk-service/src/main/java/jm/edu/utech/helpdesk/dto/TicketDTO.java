package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;
import jm.edu.utech.helpdesk.model.Priority;
import jm.edu.utech.helpdesk.model.TicketStatus;

// ticket info sent to the client
public record TicketDTO(
        Long id,
        String subject,
        String description,
        LocalDateTime creationDate,
        Priority priority,
        TicketStatus status,
        Long createdById,
        String createdByName,
        Long assignedTechnicianId,
        String assignedTechnicianName,
        String resolutionNotes,
        LocalDateTime resolvedDate,
        LocalDateTime closedDate,
        LocalDateTime lastUpdated) {
}
