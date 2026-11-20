package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// change password
public record PasswordChangeRequest(
        String currentPassword,
        String newPassword) {
}
