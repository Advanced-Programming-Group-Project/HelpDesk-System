package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jm.edu.utech.helpdesk.dto.AverageResolutionTimeDTO;
import jm.edu.utech.helpdesk.dto.TechnicianWorkloadDTO;
import jm.edu.utech.helpdesk.dto.TicketCountReport;
import jm.edu.utech.helpdesk.model.Priority;
import jm.edu.utech.helpdesk.model.Technician;
import jm.edu.utech.helpdesk.model.Ticket;
import jm.edu.utech.helpdesk.model.TicketStatus;
import jm.edu.utech.helpdesk.repository.TechnicianRepository;
import jm.edu.utech.helpdesk.repository.TicketRepository;

// reports
@Service
public class ReportService {

    private static final Logger logger = LogManager.getLogger(ReportService.class);

    private final TicketRepository ticketRepository;
    private final TechnicianRepository technicianRepository;

    @Autowired
    public ReportService(TicketRepository ticketRepository, TechnicianRepository technicianRepository) {
        this.ticketRepository = ticketRepository;
        this.technicianRepository = technicianRepository;
    }

    // tickets submitted
    public TicketCountReport ticketsSubmitted(LocalDate fromDate, LocalDate toDate) {
        checkDates(fromDate, toDate);

        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findAll()) {
            if (inRange(ticket.getCreationDate(), fromDate, toDate)) {
                tickets.add(ticket);
            }
        }

        logger.info("Report generated: Tickets Submitted (" + tickets.size() + ")");
        return buildCountReport("Tickets Submitted", fromDate, toDate, tickets);
    }

    // tickets resolved
    public TicketCountReport ticketsResolved(LocalDate fromDate, LocalDate toDate) {
        checkDates(fromDate, toDate);

        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getResolvedDate() != null && inRange(ticket.getResolvedDate(), fromDate, toDate)) {
                tickets.add(ticket);
            }
        }

        logger.info("Report generated: Tickets Resolved (" + tickets.size() + ")");
        return buildCountReport("Tickets Resolved", fromDate, toDate, tickets);
    }

    // open tickets
    public TicketCountReport openTickets() {
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getStatus().isOpen()) {
                tickets.add(ticket);
            }
        }

        logger.info("Report generated: Open Tickets (" + tickets.size() + ")");
        return buildCountReport("Open Tickets", null, null, tickets);
    }

    // technician workload
    public List<TechnicianWorkloadDTO> technicianWorkload() {
        List<TechnicianWorkloadDTO> report = new ArrayList<>();

        for (Technician technician : technicianRepository.findAll()) {
            long assigned = 0;
            long inProgress = 0;
            long pending = 0;
            long resolved = 0;
            long totalOpen = 0;

            for (Ticket ticket : ticketRepository.findByTechnician(technician.getId())) {
                switch (ticket.getStatus()) {
                    case ASSIGNED, REOPENED -> assigned++;
                    case IN_PROGRESS -> inProgress++;
                    case PENDING -> pending++;
                    case RESOLVED, CLOSED -> resolved++;
                    default -> { }
                }
                if (ticket.getStatus().isOpen()) {
                    totalOpen++;
                }
            }

            report.add(new TechnicianWorkloadDTO(technician.getId(), technician.getUser().getFullName(),
                    technician.isAvailable(), assigned, inProgress, pending, resolved, totalOpen));
        }

        logger.info("Report generated: Technician Workload (" + report.size() + " technicians)");
        return report;
    }

    // average resolution time
    public AverageResolutionTimeDTO averageResolutionTime(LocalDate fromDate, LocalDate toDate) {
        checkDates(fromDate, toDate);

        long count = 0;
        long totalMinutes = 0;

        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getResolvedDate() != null && inRange(ticket.getResolvedDate(), fromDate, toDate)) {
                totalMinutes += Duration.between(ticket.getCreationDate(), ticket.getResolvedDate()).toMinutes();
                count++;
            }
        }

        double averageMinutes = (count == 0) ? 0 : (double) totalMinutes / count;
        double averageHours = Math.round(averageMinutes / 60.0 * 100.0) / 100.0;

        logger.info("Report generated: Average Resolution Time (" + count + " tickets)");
        return new AverageResolutionTimeDTO(fromDate, toDate, count, averageHours,
                count == 0 ? "No resolved tickets" : readable(Math.round(averageMinutes)));
    }

    private TicketCountReport buildCountReport(String name, LocalDate fromDate, LocalDate toDate,
                                               List<Ticket> tickets) {
        // list every priority and status even if 0
        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority priority : Priority.values()) {
            byPriority.put(priority.name(), 0L);
        }
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            byStatus.put(status.name(), 0L);
        }

        for (Ticket ticket : tickets) {
            byPriority.put(ticket.getPriority().name(), byPriority.get(ticket.getPriority().name()) + 1);
            byStatus.put(ticket.getStatus().name(), byStatus.get(ticket.getStatus().name()) + 1);
        }

        return new TicketCountReport(name, fromDate, toDate, tickets.size(), byPriority, byStatus);
    }

    private boolean inRange(LocalDateTime dateTime, LocalDate fromDate, LocalDate toDate) {
        LocalDate date = dateTime.toLocalDate();
        if (fromDate != null && date.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && date.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private void checkDates(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("The 'from' date must be on or before the 'to' date.");
        }
    }

    // minutes to readable text
    private String readable(long minutes) {
        long days = minutes / (24 * 60);
        long hours = (minutes % (24 * 60)) / 60;
        long mins = minutes % 60;

        StringBuilder text = new StringBuilder();
        if (days > 0) text.append(days).append(days == 1 ? " day " : " days ");
        if (hours > 0) text.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (mins > 0 || text.length() == 0) text.append(mins).append(mins == 1 ? " minute" : " minutes");
        return text.toString().trim();
    }
}
