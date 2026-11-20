package jm.edu.utech.helpdesk.controller;
//created by rickayla mcneil 2111687
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jm.edu.utech.helpdesk.dto.AssignTicketRequest;
import jm.edu.utech.helpdesk.dto.CommentDTO;
import jm.edu.utech.helpdesk.dto.CommentRequest;
import jm.edu.utech.helpdesk.dto.ResolveTicketRequest;
import jm.edu.utech.helpdesk.dto.StatusUpdateRequest;
import jm.edu.utech.helpdesk.dto.TicketActionRequest;
import jm.edu.utech.helpdesk.dto.TicketCreateRequest;
import jm.edu.utech.helpdesk.dto.TicketDTO;
import jm.edu.utech.helpdesk.dto.TicketUpdateRequest;
import jm.edu.utech.helpdesk.model.Priority;
import jm.edu.utech.helpdesk.model.TicketStatus;
import jm.edu.utech.helpdesk.service.TicketService;

// ticket endpoints
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // POST /api/tickets
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDTO createTicket(@RequestBody TicketCreateRequest request) {
        return ticketService.createTicket(request);
    }

    // GET /api/tickets  (admin dashboard)
    @GetMapping
    public List<TicketDTO> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // GET /api/tickets/search?ticketId=&userId=&technicianId=&status=&priority=&from=&to=
    @GetMapping("/search")
    public List<TicketDTO> searchTickets(
            @RequestParam(name = "ticketId", required = false) Long ticketId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "technicianId", required = false) Long technicianId,
            @RequestParam(name = "status", required = false) TicketStatus status,
            @RequestParam(name = "priority", required = false) Priority priority,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ticketService.searchTickets(ticketId, userId, technicianId, status, priority, from, to);
    }

    // GET /api/tickets/user/{userId}  (user dashboard)
    @GetMapping("/user/{userId}")
    public List<TicketDTO> getTicketsByUser(@PathVariable("userId") Long userId) {
        return ticketService.getTicketsByUser(userId);
    }

    // GET /api/tickets/{id}
    @GetMapping("/{id}")
    public TicketDTO getTicket(@PathVariable("id") Long id) {
        return ticketService.getTicketById(id);
    }

    // PUT /api/tickets/{id}  (owner)
    @PutMapping("/{id}")
    public TicketDTO updateTicket(@PathVariable("id") Long id, @RequestBody TicketUpdateRequest request) {
        return ticketService.updateTicket(id, request);
    }

    // PUT /api/tickets/{id}/assign  (admin)
    @PutMapping("/{id}/assign")
    public TicketDTO assignTicket(@PathVariable("id") Long id, @RequestBody AssignTicketRequest request) {
        return ticketService.assignTicket(id, request);
    }

    // PUT /api/tickets/{id}/status  (technician)
    @PutMapping("/{id}/status")
    public TicketDTO updateStatus(@PathVariable("id") Long id, @RequestBody StatusUpdateRequest request) {
        return ticketService.updateStatus(id, request);
    }

    // PUT /api/tickets/{id}/resolve  (technician)
    @PutMapping("/{id}/resolve")
    public TicketDTO resolveTicket(@PathVariable("id") Long id, @RequestBody ResolveTicketRequest request) {
        return ticketService.resolveTicket(id, request);
    }

    // PUT /api/tickets/{id}/close  (owner or admin)
    @PutMapping("/{id}/close")
    public TicketDTO closeTicket(@PathVariable("id") Long id, @RequestBody TicketActionRequest request) {
        return ticketService.closeTicket(id, request);
    }

    // PUT /api/tickets/{id}/reopen  (owner or admin)
    @PutMapping("/{id}/reopen")
    public TicketDTO reopenTicket(@PathVariable("id") Long id, @RequestBody TicketActionRequest request) {
        return ticketService.reopenTicket(id, request);
    }

    // POST /api/tickets/{id}/comments
    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDTO addComment(@PathVariable("id") Long id, @RequestBody CommentRequest request) {
        return ticketService.addComment(id, request);
    }

    // GET /api/tickets/{id}/comments
    @GetMapping("/{id}/comments")
    public List<CommentDTO> getComments(@PathVariable("id") Long id) {
        return ticketService.getComments(id);
    }
}
