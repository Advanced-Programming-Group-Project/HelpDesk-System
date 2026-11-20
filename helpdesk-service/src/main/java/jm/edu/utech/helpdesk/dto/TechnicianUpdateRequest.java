package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// admin updates a technician
public record TechnicianUpdateRequest(
        Long adminId,
        String specialization,
        boolean available) {
}
