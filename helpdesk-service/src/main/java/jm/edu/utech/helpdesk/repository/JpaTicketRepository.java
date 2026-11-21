package jm.edu.utech.helpdesk.repository;
// member 3 (Data & Persistence)
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jm.edu.utech.helpdesk.model.Ticket;

// tickets table - written directly with JPA (Guided Lab 08)
@Repository
public class JpaTicketRepository implements TicketRepository {

    private static final Logger logger = LogManager.getLogger(JpaTicketRepository.class);

    private final EntityManagerFactory emf;

    @Autowired
    public JpaTicketRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Ticket save(Ticket ticket) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (ticket.getId() == null) {
                em.persist(ticket);
            } else {
                em.merge(ticket);
            }
            tx.commit();
            return ticket;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Database error saving ticket: " + ex.getMessage());
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Ticket.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Ticket> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select t from Ticket t order by t.id", Ticket.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Ticket> findByCreatedBy(Long userId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "select t from Ticket t where t.createdBy.id = :userId order by t.id", Ticket.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Ticket> findByTechnician(Long technicianId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "select t from Ticket t where t.assignedTechnician.id = :technicianId order by t.id",
                    Ticket.class)
                    .setParameter("technicianId", technicianId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // search by ticket id, user, technician, status, priority and date range
    // a null filter is left out of the query
    @Override
    public List<Ticket> search(TicketSearchCriteria criteria) {
        // build the JPQL text from the filters that were given
        String jpql = "select t from Ticket t where 1 = 1";
        if (criteria.ticketId() != null) {
            jpql += " and t.id = :ticketId";
        }
        if (criteria.userId() != null) {
            jpql += " and t.createdBy.id = :userId";
        }
        if (criteria.technicianId() != null) {
            jpql += " and t.assignedTechnician.id = :technicianId";
        }
        if (criteria.status() != null) {
            jpql += " and t.status = :status";
        }
        if (criteria.priority() != null) {
            jpql += " and t.priority = :priority";
        }
        if (criteria.fromDate() != null) {
            jpql += " and t.creationDate >= :fromDate";
        }
        if (criteria.toDate() != null) {
            jpql += " and t.creationDate < :toDate";
        }
        jpql += " order by t.id";

        EntityManager em = emf.createEntityManager();
        try {
            // parameter binding is safer than joining input into the query text
            TypedQuery<Ticket> query = em.createQuery(jpql, Ticket.class);
            if (criteria.ticketId() != null) {
                query.setParameter("ticketId", criteria.ticketId());
            }
            if (criteria.userId() != null) {
                query.setParameter("userId", criteria.userId());
            }
            if (criteria.technicianId() != null) {
                query.setParameter("technicianId", criteria.technicianId());
            }
            if (criteria.status() != null) {
                query.setParameter("status", criteria.status());
            }
            if (criteria.priority() != null) {
                query.setParameter("priority", criteria.priority());
            }
            if (criteria.fromDate() != null) {
                query.setParameter("fromDate", criteria.fromDate().atStartOfDay());
            }
            if (criteria.toDate() != null) {
                // up to the end of the "to" day
                query.setParameter("toDate", criteria.toDate().plusDays(1).atStartOfDay());
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
