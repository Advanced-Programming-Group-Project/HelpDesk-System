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
import jm.edu.utech.helpdesk.model.User;

// users table - written directly with JPA (Guided Lab 08)
// every write begins a transaction, commits on success, and rolls back on failure
@Repository
public class JpaUserRepository implements UserRepository {

    private static final Logger logger = LogManager.getLogger(JpaUserRepository.class);

    // Spring Boot creates one EntityManagerFactory from application.properties and injects it
    private final EntityManagerFactory emf;

    @Autowired
    public JpaUserRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // insert a new user, or update an existing one
    @Override
    public User save(User user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (user.getId() == null) {
                em.persist(user);          // INSERT - the id is filled in after commit
            } else {
                em.merge(user);            // copy the changes into the managed entity (UPDATE)
            }
            tx.commit();
            return user;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Database error saving user " + user.getUsername() + ": " + ex.getMessage());
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(User.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL uses the entity name User, not the table name users
            List<User> users = em.createQuery(
                    "select u from User u where u.username = :username", User.class)
                    .setParameter("username", username)
                    .getResultList();
            if (users.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(users.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                    "select count(u) from User u where u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                    "select count(u) from User u where lower(u.email) = lower(:email)", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select u from User u order by u.id", User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
