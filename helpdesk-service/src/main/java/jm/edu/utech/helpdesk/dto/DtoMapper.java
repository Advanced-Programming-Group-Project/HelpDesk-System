package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import org.springframework.stereotype.Component;

import jm.edu.utech.helpdesk.model.Comment;
import jm.edu.utech.helpdesk.model.Technician;
import jm.edu.utech.helpdesk.model.Ticket;
import jm.edu.utech.helpdesk.model.User;

// converts model objects into DTOs
@Component
public class DtoMapper {

    public UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt());
    }

    public TechnicianDTO toTechnicianDTO(Technician technician, int activeTickets) {
        User user = technician.getUser();
        return new TechnicianDTO(technician.getId(), user.getId(), user.getUsername(),
                user.getFullName(), user.getEmail(), technician.getSpecialization(),
                technician.isAvailable(), activeTickets);
    }

    public TicketDTO toTicketDTO(Ticket ticket) {
        Technician technician = ticket.getAssignedTechnician();

        return new TicketDTO(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCreationDate(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getFullName(),
                technician == null ? null : technician.getId(),
                technician == null ? null : technician.getUser().getFullName(),
                ticket.getResolutionNotes(),
                ticket.getResolvedDate(),
                ticket.getClosedDate(),
                ticket.getLastUpdated());
    }

    public CommentDTO toCommentDTO(Comment comment) {
        User author = comment.getAuthor();
        return new CommentDTO(comment.getId(), comment.getTicket().getId(), author.getId(),
                author.getFullName(), author.getRole(), comment.getText(), comment.getCreatedAt());
    }
}
