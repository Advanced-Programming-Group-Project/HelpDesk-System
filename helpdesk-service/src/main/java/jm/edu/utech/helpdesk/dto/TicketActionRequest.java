package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// close or reopen a ticket
public record TicketActionRequest(
        Long userId,
        String reason) {
}
