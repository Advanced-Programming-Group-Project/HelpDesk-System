package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// update name and email
public record UserUpdateRequest(
        String fullName,
        String email) {
}
