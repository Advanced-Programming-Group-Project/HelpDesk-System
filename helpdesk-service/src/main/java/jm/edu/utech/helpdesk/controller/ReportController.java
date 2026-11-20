package jm.edu.utech.helpdesk.controller;
//created by rickayla mcneil 2111687
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jm.edu.utech.helpdesk.dto.AverageResolutionTimeDTO;
import jm.edu.utech.helpdesk.dto.TechnicianWorkloadDTO;
import jm.edu.utech.helpdesk.dto.TicketCountReport;
import jm.edu.utech.helpdesk.service.ReportService;

// report endpoints
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // GET /api/reports/tickets-submitted?from=&to=
    @GetMapping("/tickets-submitted")
    public TicketCountReport ticketsSubmitted(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.ticketsSubmitted(from, to);
    }

    // GET /api/reports/tickets-resolved?from=&to=
    @GetMapping("/tickets-resolved")
    public TicketCountReport ticketsResolved(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.ticketsResolved(from, to);
    }

    // GET /api/reports/open-tickets
    @GetMapping("/open-tickets")
    public TicketCountReport openTickets() {
        return reportService.openTickets();
    }

    // GET /api/reports/technician-workload
    @GetMapping("/technician-workload")
    public List<TechnicianWorkloadDTO> technicianWorkload() {
        return reportService.technicianWorkload();
    }

    // GET /api/reports/average-resolution-time?from=&to=
    @GetMapping("/average-resolution-time")
    public AverageResolutionTimeDTO averageResolutionTime(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.averageResolutionTime(from, to);
    }
}
