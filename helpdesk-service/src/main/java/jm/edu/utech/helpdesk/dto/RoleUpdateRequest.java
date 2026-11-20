package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
import jm.edu.utech.helpdesk.model.Role;

// admin changes a user's role
public record RoleUpdateRequest(
        Long adminId,
        Role role) {
}
