package jm.edu.utech.helpdesk.repository;
// member 3 (Data & Persistence)
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jm.edu.utech.helpdesk.model.Comment;

// comments table - written directly with JPA (Guided Lab 08)
@Repository
public class JpaCommentRepository implements CommentRepository {

    private static final Logger logger = LogManager.getLogger(JpaCommentRepository.class);

    private final EntityManagerFactory emf;

    @Autowired
    public JpaCommentRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Comment save(Comment comment) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (comment.getId() == null) {
                em.persist(comment);
            } else {
                em.merge(comment);
            }
            tx.commit();
            return comment;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Database error saving comment: " + ex.getMessage());
            throw ex;
        } finally {
            em.close();
        }
    }

    // oldest first
    @Override
    public List<Comment> findByTicketId(Long ticketId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "select c from Comment c where c.ticket.id = :ticketId order by c.createdAt, c.id",
                    Comment.class)
                    .setParameter("ticketId", ticketId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
