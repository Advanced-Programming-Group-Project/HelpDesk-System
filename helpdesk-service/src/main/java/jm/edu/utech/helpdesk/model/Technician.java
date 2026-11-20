package jm.edu.utech.helpdesk.model;
//created by rickayla mcneil 2111687
// a technician, linked to their user account
// member 3 input: JPA annotations
public class Technician {

    private Long id;
    private User user;
    private String specialization;   // e.g. Networking, Hardware
    private boolean available;

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
}
