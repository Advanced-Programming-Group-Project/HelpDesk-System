package jm.edu.utech.helpdesk.repository;
//created by rickayla mcneil 2111687
import java.util.List;
import java.util.Optional;

import jm.edu.utech.helpdesk.model.Ticket;

// tickets table
// member 3 input: implement with JPA + @Repository
public interface TicketRepository {

    public abstract Ticket save(Ticket ticket);

    public abstract Optional<Ticket> findById(Long id);

    public abstract List<Ticket> findAll();

    public abstract List<Ticket> findByCreatedBy(Long userId);

    public abstract List<Ticket> findByTechnician(Long technicianId);

    // member 3 input: JPQL search query
    public abstract List<Ticket> search(TicketSearchCriteria criteria);
}
