package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// admin activates or deactivates an account
public record AccountStatusRequest(
        Long adminId,
        boolean active) {
}
