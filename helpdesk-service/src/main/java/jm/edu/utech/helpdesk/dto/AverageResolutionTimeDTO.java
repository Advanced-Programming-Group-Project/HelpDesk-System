package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDate;

// average resolution time report
public record AverageResolutionTimeDTO(
        LocalDate fromDate,
        LocalDate toDate,
        long ticketsResolved,
        double averageHours,
        String averageReadable) {
}
