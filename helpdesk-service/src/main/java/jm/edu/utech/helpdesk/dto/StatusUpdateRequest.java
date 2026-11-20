package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import jm.edu.utech.helpdesk.model.TicketStatus;

// technician changes ticket status
public record StatusUpdateRequest(
        Long technicianId,
        TicketStatus status) {
}
