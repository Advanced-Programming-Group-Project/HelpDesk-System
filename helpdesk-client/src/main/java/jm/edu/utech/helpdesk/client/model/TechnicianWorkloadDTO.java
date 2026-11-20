package jm.edu.utech.helpdesk.client.model;
// Member 1 (Front-End)

// technician workload report row
public record TechnicianWorkloadDTO(
        Long technicianId,
        String technicianName,
        boolean available,
        long assigned,
        long inProgress,
        long pending,
        long resolved,
        long totalOpen) {
}
