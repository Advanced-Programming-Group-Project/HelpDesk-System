package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import java.time.LocalDate;
import java.util.Map;

// ticket count report
public record TicketCountReport(
        String reportName,
        LocalDate fromDate,
        LocalDate toDate,
        long total,
        Map<String, Long> byPriority,
        Map<String, Long> byStatus) {
}
