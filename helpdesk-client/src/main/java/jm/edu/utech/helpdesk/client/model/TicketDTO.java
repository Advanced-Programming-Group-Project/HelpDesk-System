package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDateTime;

// ticket info received from the server
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
