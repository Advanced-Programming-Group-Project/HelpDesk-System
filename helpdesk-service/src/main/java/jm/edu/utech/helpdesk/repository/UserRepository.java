package jm.edu.utech.helpdesk.repository;
//created by rickayla mcneil 2111687
import java.util.List;
import java.util.Optional;

import jm.edu.utech.helpdesk.model.User;

// users table
// member 3 input: implement with JPA + @Repository, and add a default ADMIN account
public interface UserRepository {

    // insert or update
    public abstract User save(User user);

    public abstract Optional<User> findById(Long id);

    public abstract Optional<User> findByUsername(String username);

    public abstract boolean existsByUsername(String username);

    public abstract boolean existsByEmail(String email);

    public abstract List<User> findAll();
}
