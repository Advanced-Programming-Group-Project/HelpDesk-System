package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jm.edu.utech.helpdesk.dto.AccountStatusRequest;
import jm.edu.utech.helpdesk.dto.DtoMapper;
import jm.edu.utech.helpdesk.dto.LoginRequest;
import jm.edu.utech.helpdesk.dto.MessageResponse;
import jm.edu.utech.helpdesk.dto.PasswordChangeRequest;
import jm.edu.utech.helpdesk.dto.RoleUpdateRequest;
import jm.edu.utech.helpdesk.dto.UserDTO;
import jm.edu.utech.helpdesk.dto.UserRegistrationRequest;
import jm.edu.utech.helpdesk.dto.UserUpdateRequest;
import jm.edu.utech.helpdesk.exception.DuplicateUserException;
import jm.edu.utech.helpdesk.exception.InvalidCredentialsException;
import jm.edu.utech.helpdesk.exception.NotAuthorizedException;
import jm.edu.utech.helpdesk.exception.UserNotFoundException;
import jm.edu.utech.helpdesk.model.Role;
import jm.edu.utech.helpdesk.model.Technician;
import jm.edu.utech.helpdesk.model.User;
import jm.edu.utech.helpdesk.repository.TechnicianRepository;
import jm.edu.utech.helpdesk.repository.UserRepository;

// user management
@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final PasswordHasher passwordHasher;
    private final DtoMapper mapper;

    // constructor injection
    @Autowired
    public UserService(UserRepository userRepository, TechnicianRepository technicianRepository,
                       PasswordHasher passwordHasher, DtoMapper mapper) {
        this.userRepository = userRepository;
        this.technicianRepository = technicianRepository;
        this.passwordHasher = passwordHasher;
        this.mapper = mapper;
    }

    // register a student/staff account
    public UserDTO register(UserRegistrationRequest request) {
        InputValidator.required(request, "Registration details");

        String username = InputValidator.username(request.username());
        InputValidator.password(request.password());
        String fullName = InputValidator.required(request.fullName(), "Full name", 100);
        String email = InputValidator.email(request.email());

        // synchronized so the same username can't be registered twice at once
        synchronized (this) {
            if (userRepository.existsByUsername(username)) {
                logger.warn("Registration refused - username already exists: " + username);
                throw new DuplicateUserException("The username '" + username + "' is already taken.");
            }
            if (userRepository.existsByEmail(email)) {
                logger.warn("Registration refused - email already exists: " + email);
                throw new DuplicateUserException("An account with the email " + email + " already exists.");
            }

            User user = new User(username, passwordHasher.hash(request.password()), fullName, email, Role.USER);
            userRepository.save(user);

            logger.info("User registered: " + username + " (id " + user.getId() + ")");
            return mapper.toUserDTO(user);
        }
    }

    public UserDTO login(LoginRequest request) {
        InputValidator.required(request, "Login details");

        if (request.username() == null || request.password() == null) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        Optional<User> found = userRepository.findByUsername(request.username().trim());

        if (found.isEmpty() || !passwordHasher.matches(request.password(), found.get().getPasswordHash())) {
            logger.warn("Invalid login attempt for username: " + request.username());
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        User user = found.get();

        if (!user.isActive()) {
            logger.warn("Login attempt on deactivated account: " + user.getUsername());
            throw new InvalidCredentialsException("This account has been deactivated. Contact IT Services.");
        }

        logger.info("User Login: " + user.getUsername() + " (" + user.getRole() + ")");
        return mapper.toUserDTO(user);
    }

    public MessageResponse logout(Long userId) {
        User user = getUser(userId);
        logger.info("User Logout: " + user.getUsername());
        return new MessageResponse("Logged out successfully.", LocalDateTime.now());
    }

    public UserDTO getUserById(Long id) {
        return mapper.toUserDTO(getUser(id));
    }

    public List<UserDTO> getAllUsers() {
        List<UserDTO> result = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            result.add(mapper.toUserDTO(user));
        }
        return result;
    }

    public UserDTO updateUser(Long id, UserUpdateRequest request) {
        InputValidator.required(request, "User details");
        User user = getUser(id);

        String fullName = InputValidator.required(request.fullName(), "Full name", 100);
        String email = InputValidator.email(request.email());

        synchronized (this) {
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new DuplicateUserException("An account with the email " + email + " already exists.");
            }
            user.setFullName(fullName);
            user.setEmail(email);
            userRepository.save(user);
        }

        logger.info("User updated: " + user.getUsername());
        return mapper.toUserDTO(user);
    }

    public MessageResponse changePassword(Long id, PasswordChangeRequest request) {
        InputValidator.required(request, "Password details");
        User user = getUser(id);

        if (!passwordHasher.matches(request.currentPassword(), user.getPasswordHash())) {
            logger.warn("Password change refused - wrong current password for " + user.getUsername());
            throw new InvalidCredentialsException("The current password is incorrect.");
        }

        InputValidator.password(request.newPassword());
        user.setPasswordHash(passwordHasher.hash(request.newPassword()));
        userRepository.save(user);

        logger.info("Password changed: " + user.getUsername());
        return new MessageResponse("Password changed successfully.", LocalDateTime.now());
    }

    // admin changes a role
    public UserDTO changeRole(Long id, RoleUpdateRequest request) {
        InputValidator.required(request, "Role details");
        User admin = requireAdmin(request.adminId());
        Role newRole = InputValidator.required(request.role(), "Role");
        User user = getUser(id);

        if (newRole == Role.TECHNICIAN) {
            throw new IllegalArgumentException("To make a user a technician, use POST /api/technicians.");
        }
        if (user.getId().equals(admin.getId()) && newRole != Role.ADMIN) {
            throw new IllegalArgumentException("You cannot remove your own administrator role.");
        }

        // technician with another role can't get tickets
        if (user.getRole() == Role.TECHNICIAN) {
            Optional<Technician> technician = technicianRepository.findByUserId(user.getId());
            if (technician.isPresent()) {
                technician.get().setAvailable(false);
                technicianRepository.save(technician.get());
            }
        }

        user.setRole(newRole);
        userRepository.save(user);

        logger.info("Role changed: " + user.getUsername() + " is now " + newRole + " (by " + admin.getUsername() + ")");
        return mapper.toUserDTO(user);
    }

    public UserDTO setAccountStatus(Long id, AccountStatusRequest request) {
        InputValidator.required(request, "Account status");
        User admin = requireAdmin(request.adminId());
        User user = getUser(id);

        if (user.getId().equals(admin.getId()) && !request.active()) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }

        user.setActive(request.active());
        userRepository.save(user);

        logger.info("Account " + (request.active() ? "activated" : "deactivated") + ": "
                + user.getUsername() + " (by " + admin.getUsername() + ")");
        return mapper.toUserDTO(user);
    }

    // get user or throw
    public User getUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User " + id + " was not found."));
    }

    // must be an active admin
    public User requireAdmin(Long userId) {
        User user = getUser(userId);
        if (user.getRole() != Role.ADMIN || !user.isActive()) {
            logger.warn("Administrator action refused for user " + user.getUsername());
            throw new NotAuthorizedException("Only an administrator can do this.");
        }
        return user;
    }

    // must be active
    public User requireActiveUser(Long userId) {
        User user = getUser(userId);
        if (!user.isActive()) {
            throw new NotAuthorizedException("This account has been deactivated.");
        }
        return user;
    }
}
