package jm.edu.utech.helpdesk.dto;
//created by rickayla mcneil 2111687
// register
public record UserRegistrationRequest(
        String username,
        String password,
        String fullName,
        String email) {
}
