package jm.edu.utech.helpdesk.controller;
//created by rickayla mcneil 2111687
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jm.edu.utech.helpdesk.dto.AccountStatusRequest;
import jm.edu.utech.helpdesk.dto.LoginRequest;
import jm.edu.utech.helpdesk.dto.LogoutRequest;
import jm.edu.utech.helpdesk.dto.MessageResponse;
import jm.edu.utech.helpdesk.dto.PasswordChangeRequest;
import jm.edu.utech.helpdesk.dto.RoleUpdateRequest;
import jm.edu.utech.helpdesk.dto.UserDTO;
import jm.edu.utech.helpdesk.dto.UserRegistrationRequest;
import jm.edu.utech.helpdesk.dto.UserUpdateRequest;
import jm.edu.utech.helpdesk.service.UserService;

// user endpoints
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // constructor injection
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users/register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    // POST /api/users/login
    @PostMapping("/login")
    public UserDTO login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // POST /api/users/logout
    @PostMapping("/logout")
    public MessageResponse logout(@RequestBody LogoutRequest request) {
        return userService.logout(request.userId());
    }

    // GET /api/users
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    // PUT /api/users/{id}/password
    @PutMapping("/{id}/password")
    public MessageResponse changePassword(@PathVariable("id") Long id, @RequestBody PasswordChangeRequest request) {
        return userService.changePassword(id, request);
    }

    // PUT /api/users/{id}/role  (admin)
    @PutMapping("/{id}/role")
    public UserDTO changeRole(@PathVariable("id") Long id, @RequestBody RoleUpdateRequest request) {
        return userService.changeRole(id, request);
    }

    // PUT /api/users/{id}/status  (admin)
    @PutMapping("/{id}/status")
    public UserDTO setAccountStatus(@PathVariable("id") Long id, @RequestBody AccountStatusRequest request) {
        return userService.setAccountStatus(id, request);
    }
}
