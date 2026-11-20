package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// technician resolves a ticket
public record ResolveTicketRequest(
        Long technicianId,
        String resolutionNotes) {
}
