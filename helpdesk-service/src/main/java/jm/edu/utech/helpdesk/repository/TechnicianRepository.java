package jm.edu.utech.helpdesk.repository;
//created by rickayla mcneil 2111687
import java.util.List;
import java.util.Optional;

import jm.edu.utech.helpdesk.model.Technician;

// technicians table
// member 3 input: implement with JPA + @Repository
public interface TechnicianRepository {

    public abstract Technician save(Technician technician);

    public abstract Optional<Technician> findById(Long id);

    public abstract Optional<Technician> findByUserId(Long userId);

    public abstract List<Technician> findAll();
}
