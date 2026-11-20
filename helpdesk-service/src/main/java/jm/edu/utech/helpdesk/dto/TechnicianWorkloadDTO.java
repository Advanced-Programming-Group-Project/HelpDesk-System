package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// technician workload report
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
