package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDate;

// average resolution time report
public record AverageResolutionTimeDTO(
        LocalDate fromDate,
        LocalDate toDate,
        long ticketsResolved,
        double averageHours,
        String averageReadable) {
}
