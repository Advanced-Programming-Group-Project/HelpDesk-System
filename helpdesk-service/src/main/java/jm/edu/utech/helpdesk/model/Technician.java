package jm.edu.utech.helpdesk.model;
//created by rickayla mcneil 2111687
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

// a technician, linked to their user account
// member 3: JPA entity mapped to the technicians table
@Entity
@Table(name = "technicians")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "technician_id")
    private Long id;

    // One-to-One: each technician is linked to one user account
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100)
    private String specialization;   // e.g. Networking, Hardware

    @Column(nullable = false)
    private boolean available;

    // One-to-Many: one technician is assigned many tickets
    @OneToMany(mappedBy = "assignedTechnician")
    private List<Ticket> tickets = new ArrayList<>();

    public Technician() {
    }

    public Technician(User user, String specialization) {
        this.user = user;
        this.specialization = specialization;
        this.available = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // tickets assigned to this technician (only loaded while the EntityManager is open)
    public List<Ticket> getTickets() {
        return tickets;
    }
}
