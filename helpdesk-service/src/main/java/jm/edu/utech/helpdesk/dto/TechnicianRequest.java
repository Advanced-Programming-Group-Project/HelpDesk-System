package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// admin makes a user a technician
public record TechnicianRequest(
        Long adminId,
        Long userId,
        String specialization) {
}
