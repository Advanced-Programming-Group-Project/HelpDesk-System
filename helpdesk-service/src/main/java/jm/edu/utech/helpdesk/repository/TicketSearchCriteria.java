package jm.edu.utech.helpdesk.repository;
//created by rickayla mcneil 2111687
import java.time.LocalDate;

import jm.edu.utech.helpdesk.model.Priority;
import jm.edu.utech.helpdesk.model.TicketStatus;

// search filters - null means not used
public record TicketSearchCriteria(
        Long ticketId,
        Long userId,
        Long technicianId,
        TicketStatus status,
        Priority priority,
        LocalDate fromDate,
        LocalDate toDate) {
}
