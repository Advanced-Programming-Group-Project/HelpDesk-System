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
import jm.edu.utech.helpdesk.model.Technician;

// technicians table - written directly with JPA (Guided Lab 08)
@Repository
public class JpaTechnicianRepository implements TechnicianRepository {

    private static final Logger logger = LogManager.getLogger(JpaTechnicianRepository.class);

    private final EntityManagerFactory emf;

    @Autowired
    public JpaTechnicianRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Technician save(Technician technician) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (technician.getId() == null) {
                em.persist(technician);
            } else {
                em.merge(technician);
            }
            tx.commit();
            return technician;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Database error saving technician: " + ex.getMessage());
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Technician> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Technician.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Technician> findByUserId(Long userId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Technician> technicians = em.createQuery(
                    "select t from Technician t where t.user.id = :userId", Technician.class)
                    .setParameter("userId", userId)
                    .getResultList();
            if (technicians.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(technicians.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Technician> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select t from Technician t order by t.id", Technician.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
