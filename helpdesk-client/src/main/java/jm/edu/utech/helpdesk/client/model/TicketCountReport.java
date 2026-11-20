package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

import java.time.LocalDate;
import java.util.Map;

// tickets submitted / resolved / open report
public record TicketCountReport(
        String reportName,
        LocalDate fromDate,
        LocalDate toDate,
        long total,
        Map<String, Long> byPriority,
        Map<String, Long> byStatus) {
}
