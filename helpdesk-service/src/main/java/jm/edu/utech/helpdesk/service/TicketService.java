package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jm.edu.utech.helpdesk.dto.AssignTicketRequest;
import jm.edu.utech.helpdesk.dto.CommentDTO;
import jm.edu.utech.helpdesk.dto.CommentRequest;
import jm.edu.utech.helpdesk.dto.DtoMapper;
import jm.edu.utech.helpdesk.dto.ResolveTicketRequest;
import jm.edu.utech.helpdesk.dto.StatusUpdateRequest;
import jm.edu.utech.helpdesk.dto.TicketActionRequest;
import jm.edu.utech.helpdesk.dto.TicketCreateRequest;
import jm.edu.utech.helpdesk.dto.TicketDTO;
import jm.edu.utech.helpdesk.dto.TicketUpdateRequest;
import jm.edu.utech.helpdesk.exception.InvalidStatusChangeException;
import jm.edu.utech.helpdesk.exception.NotAuthorizedException;
import jm.edu.utech.helpdesk.exception.TicketAssignmentException;
import jm.edu.utech.helpdesk.exception.TicketNotFoundException;
import jm.edu.utech.helpdesk.model.Comment;
import jm.edu.utech.helpdesk.model.Priority;
import jm.edu.utech.helpdesk.model.Role;
import jm.edu.utech.helpdesk.model.Technician;
import jm.edu.utech.helpdesk.model.Ticket;
import jm.edu.utech.helpdesk.model.TicketStatus;
import jm.edu.utech.helpdesk.model.User;
import jm.edu.utech.helpdesk.repository.CommentRepository;
import jm.edu.utech.helpdesk.repository.TicketRepository;
import jm.edu.utech.helpdesk.repository.TicketSearchCriteria;

// ticket management
@Service
public class TicketService {

    private static final Logger logger = LogManager.getLogger(TicketService.class);

    private final Lock ticketLock = new ReentrantLock();

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final TechnicianService technicianService;
    private final NotificationService notificationService;
    private final DtoMapper mapper;

    @Autowired
    public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository,
                         UserService userService, TechnicianService technicianService,
                         NotificationService notificationService, DtoMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.technicianService = technicianService;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    public TicketDTO createTicket(TicketCreateRequest request) {
        InputValidator.required(request, "Ticket details");
        User user = userService.requireActiveUser(request.userId());

        String subject = InputValidator.required(request.subject(), "Subject", 150);
        String description = InputValidator.required(request.description(), "Description", 2000);
        Priority priority = InputValidator.required(request.priority(), "Priority");

        Ticket ticket = new Ticket(subject, description, priority, user);
        ticketRepository.save(ticket);

        logger.info("Ticket created: #" + ticket.getId() + " '" + subject + "' (" + priority
                + ") by " + user.getUsername());

        if (priority == Priority.URGENT) {
            notifySafely(() -> notificationService.urgentTicketCreated(ticket), ticket);
        }

        return mapper.toTicketDTO(ticket);
    }

    public TicketDTO getTicketById(Long id) {
        return mapper.toTicketDTO(getTicket(id));
    }

    public List<TicketDTO> getAllTickets() {
        return toDTOs(ticketRepository.findAll());
    }

    public List<TicketDTO> getTicketsByUser(Long userId) {
        userService.getUser(userId);  // checks the user exists
        return toDTOs(ticketRepository.findByCreatedBy(userId));
    }

    // search tickets
    public List<TicketDTO> searchTickets(Long ticketId, Long userId, Long technicianId,
                                         TicketStatus status, Priority priority,
                                         LocalDate fromDate, LocalDate toDate) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("The 'from' date must be on or before the 'to' date.");
        }

        TicketSearchCriteria criteria = new TicketSearchCriteria(ticketId, userId, technicianId,
                status, priority, fromDate, toDate);
        return toDTOs(ticketRepository.search(criteria));
    }

    // owner updates the ticket
    public TicketDTO updateTicket(Long id, TicketUpdateRequest request) {
        InputValidator.required(request, "Ticket details");
        User user = userService.requireActiveUser(request.userId());

        String subject = InputValidator.required(request.subject(), "Subject", 150);
        String description = InputValidator.required(request.description(), "Description", 2000);
        Priority priority = InputValidator.required(request.priority(), "Priority");

        ticketLock.lock();
        try {
            Ticket ticket = getTicket(id);
            requireOwnerOrAdmin(ticket, user);

            if (!ticket.getStatus().isOpen()) {
                throw new InvalidStatusChangeException("A " + ticket.getStatus()
                        + " ticket cannot be edited. Reopen it first.");
            }

            boolean becameUrgent = priority == Priority.URGENT && ticket.getPriority() != Priority.URGENT;

            ticket.setSubject(subject);
            ticket.setDescription(description);
            ticket.setPriority(priority);
            ticket.setLastUpdated(LocalDateTime.now());
            ticketRepository.save(ticket);

            logger.info("Ticket updated: #" + id + " by " + user.getUsername());

            if (becameUrgent) {
                notifySafely(() -> notificationService.urgentTicketCreated(ticket), ticket);
            }
            return mapper.toTicketDTO(ticket);
        } finally {
            ticketLock.unlock();
        }
    }

    public TicketDTO closeTicket(Long id, TicketActionRequest request) {
        InputValidator.required(request, "Close details");
        User user = userService.requireActiveUser(request.userId());

        ticketLock.lock();
        try {
            Ticket ticket = getTicket(id);
            requireOwnerOrAdmin(ticket, user);
            changeStatus(ticket, TicketStatus.CLOSED);
            ticket.setClosedDate(LocalDateTime.now());
            ticketRepository.save(ticket);

            addSystemComment(ticket, user, "Ticket closed", request.reason());
            logger.info("Ticket closed: #" + id + " by " + user.getUsername());
            return mapper.toTicketDTO(ticket);
        } finally {
            ticketLock.unlock();
        }
    }

    public TicketDTO reopenTicket(Long id, TicketActionRequest request) {
        InputValidator.required(request, "Reopen details");
        User user = userService.requireActiveUser(request.userId());

        ticketLock.lock();
        try {
            Ticket ticket = getTicket(id);
            requireOwnerOrAdmin(ticket, user);
            changeStatus(ticket, TicketStatus.REOPENED);
            ticket.setResolvedDate(null);
            ticket.setClosedDate(null);
            ticketRepository.save(ticket);

            addSystemComment(ticket, user, "Ticket reopened", request.reason());
            logger.info("Ticket reopened: #" + id + " by " + user.getUsername());
            return mapper.toTicketDTO(ticket);
        } finally {
            ticketLock.unlock();
        }
    }

    // admin assigns a ticket - locked so two admins can't assign it at once
    public TicketDTO assignTicket(Long id, AssignTicketRequest request) {
        InputValidator.required(request, "Assignment details");
        User admin = userService.requireAdmin(request.adminId());
        Technician technician = technicianService.getTechnician(request.technicianId());

        if (!technician.isAvailable() || !technician.getUser().isActive()
                || technician.getUser().getRole() != Role.TECHNICIAN) {
            throw new TicketAssignmentException(technician.getUser().getFullName()
                    + " is not available to receive tickets.");
        }

        ticketLock.lock();
        try {
            Ticket ticket = getTicket(id);
            TicketStatus status = ticket.getStatus();
            Technician current = ticket.getAssignedTechnician();

            if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
                throw new TicketAssignmentException("Ticket #" + id + " is " + status
                        + " and cannot be assigned.");
            }

            boolean alreadyAssigned = current != null && status != TicketStatus.NEW;

            if (alreadyAssigned && status != TicketStatus.REOPENED
                    && current.getId().equals(technician.getId())) {
                logger.warn("Assignment refused: ticket #" + id + " already assigned to "
                        + technician.getUser().getFullName() + " (attempt by " + admin.getUsername() + ")");
                throw new TicketAssignmentException("Ticket #" + id + " is already assigned to "
                        + technician.getUser().getFullName() + ".");
            }

            if (alreadyAssigned && status != TicketStatus.REOPENED && !request.reassign()) {
                logger.warn("Assignment refused: ticket #" + id + " already assigned to "
                        + current.getUser().getFullName() + " (attempt by " + admin.getUsername() + ")");
                throw new TicketAssignmentException("Ticket #" + id + " has already been assigned to "
                        + current.getUser().getFullName() + ". Set reassign to true to move it.");
            }

            ticket.setAssignedTechnician(technician);
            ticket.setStatus(TicketStatus.ASSIGNED);
            ticket.setLastUpdated(LocalDateTime.now());
            ticketRepository.save(ticket);

            logger.info("Ticket assigned: #" + id + " to " + technician.getUser().getUsername()
                    + " by " + admin.getUsername());

            notifySafely(() -> notificationService.ticketAssigned(ticket), ticket);
            return mapper.toTicketDTO(ticket);
        } finally {
            ticketLock.unlock();
        }
    }

    // technician sets IN_PROGRESS or PENDING
    public TicketDTO updateStatus(Long id, StatusUpdateRequest request) {
        InputValidator.required(request, "Status details");
        TicketStatus newStatus = InputValidator.required(request.status(), "Status");

        if (newStatus != TicketStatus.IN_PROGRESS && newStatus != TicketStatus.PENDING) {
            throw new IllegalArgumentException("Technicians can set the status to IN_PROGRESS or PENDING. "
                    + "Use /resolve to resolve a ticket.");
        }

        ticketLock.lock();
        try {
            Ticket ticket = getTicket(id);
            Technician technician = requireAssignedTechnician(ticket, request.technicianId());

            TicketStatus oldStatus = ticket.getStatus();
            changeStatus(ticket, newStatus);
            ticketRepository.save(ticket);

            logger.info("Ticket status: #" + id + " " + oldStatus + " -> " + newStatus
                    + " by " + technician.getUser().getUsername());
            return mapper.toTicketDTO(ticket);
        } finally {
            ticketLock.unlock();
        }
    }

    public TicketDTO resolveTicket(Long id, ResolveTicketRequest request) {
        InputValidator.required(request, "Resolution details");
        String notes = InputValidator.required(request.resolutionNotes(), "Resolution notes", 2000);

        ticketLock.lock();
        try {
            Ticket ticket = getTicket(id);
            Technician technician = requireAssignedTechnician(ticket, request.technicianId());

            changeStatus(ticket, TicketStatus.RESOLVED);
            ticket.setResolutionNotes(notes);
            ticket.setResolvedDate(LocalDateTime.now());
            ticketRepository.save(ticket);

            logger.info("Ticket resolved: #" + id + " by " + technician.getUser().getUsername());

            notifySafely(() -> notificationService.ticketResolved(ticket), ticket);
            return mapper.toTicketDTO(ticket);
        } finally {
            ticketLock.unlock();
        }
    }

    // add a comment
    public CommentDTO addComment(Long ticketId, CommentRequest request) {
        InputValidator.required(request, "Comment");
        User author = userService.requireActiveUser(request.authorId());
        String text = InputValidator.required(request.text(), "Comment text", 1000);

        Ticket ticket = getTicket(ticketId);

        boolean isOwner = ticket.getCreatedBy().getId().equals(author.getId());
        boolean isAssignedTechnician = ticket.getAssignedTechnician() != null
                && ticket.getAssignedTechnician().getUser().getId().equals(author.getId());
        boolean isAdmin = author.getRole() == Role.ADMIN;

        if (!isOwner && !isAssignedTechnician && !isAdmin) {
            throw new NotAuthorizedException("You can only comment on your own tickets or tickets assigned to you.");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidStatusChangeException("Comments cannot be added to a CLOSED ticket. Reopen it first.");
        }

        Comment comment = new Comment(ticket, author, text);
        commentRepository.save(comment);

        logger.info("Comment added to ticket #" + ticketId + " by " + author.getUsername());

        notifySafely(() -> notificationService.commentAdded(ticket, comment), ticket);
        return mapper.toCommentDTO(comment);
    }

    public List<CommentDTO> getComments(Long ticketId) {
        getTicket(ticketId);  // checks the ticket exists

        List<CommentDTO> result = new ArrayList<>();
        for (Comment comment : commentRepository.findByTicketId(ticketId)) {
            result.add(mapper.toCommentDTO(comment));
        }
        return result;
    }

    public Ticket getTicket(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Ticket ID is required.");
        }
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket " + id + " was not found."));
    }

    private void changeStatus(Ticket ticket, TicketStatus newStatus) {
        if (!ticket.getStatus().canChangeTo(newStatus)) {
            throw new InvalidStatusChangeException("Ticket #" + ticket.getId() + " cannot change from "
                    + ticket.getStatus() + " to " + newStatus + ".");
        }
        ticket.setStatus(newStatus);
        ticket.setLastUpdated(LocalDateTime.now());
    }

    private void requireOwnerOrAdmin(Ticket ticket, User user) {
        boolean isOwner = ticket.getCreatedBy().getId().equals(user.getId());
        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new NotAuthorizedException("Only the person who created ticket #" + ticket.getId()
                    + " or an administrator can do this.");
        }
    }

    private Technician requireAssignedTechnician(Ticket ticket, Long technicianId) {
        Technician technician = technicianService.getTechnician(technicianId);

        if (ticket.getAssignedTechnician() == null
                || !ticket.getAssignedTechnician().getId().equals(technician.getId())) {
            throw new NotAuthorizedException("Ticket #" + ticket.getId() + " is not assigned to you.");
        }
        if (!technician.getUser().isActive()) {
            throw new NotAuthorizedException("This account has been deactivated.");
        }
        return technician;
    }

    // saves close/reopen in the comments
    private void addSystemComment(Ticket ticket, User user, String action, String reason) {
        String text = action + (reason == null || reason.isBlank() ? "." : ": " + reason.trim());
        if (text.length() > 1000) {
            text = text.substring(0, 1000);
        }
        commentRepository.save(new Comment(ticket, user, text));
    }

    // a failed notification won't stop the ticket change
    private void notifySafely(Runnable notification, Ticket ticket) {
        try {
            notification.run();
        } catch (Exception ex) {
            logger.error("Notification failure for ticket #" + ticket.getId(), ex);
        }
    }

    private List<TicketDTO> toDTOs(List<Ticket> tickets) {
        List<TicketDTO> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            result.add(mapper.toTicketDTO(ticket));
        }
        return result;
    }
}
