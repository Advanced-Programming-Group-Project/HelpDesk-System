package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jm.edu.utech.helpdesk.dto.DtoMapper;
import jm.edu.utech.helpdesk.dto.TechnicianDTO;
import jm.edu.utech.helpdesk.dto.TechnicianRequest;
import jm.edu.utech.helpdesk.dto.TechnicianUpdateRequest;
import jm.edu.utech.helpdesk.dto.TicketDTO;
import jm.edu.utech.helpdesk.exception.TechnicianNotFoundException;
import jm.edu.utech.helpdesk.model.Role;
import jm.edu.utech.helpdesk.model.Technician;
import jm.edu.utech.helpdesk.model.Ticket;
import jm.edu.utech.helpdesk.model.User;
import jm.edu.utech.helpdesk.repository.TechnicianRepository;
import jm.edu.utech.helpdesk.repository.TicketRepository;
import jm.edu.utech.helpdesk.repository.UserRepository;

// technician management
@Service
public class TechnicianService {

    private static final Logger logger = LogManager.getLogger(TechnicianService.class);

    private final TechnicianRepository technicianRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final DtoMapper mapper;

    @Autowired
    public TechnicianService(TechnicianRepository technicianRepository, TicketRepository ticketRepository,
                             UserRepository userRepository, UserService userService, DtoMapper mapper) {
        this.technicianRepository = technicianRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.mapper = mapper;
    }

    // admin makes a user a technician
    public synchronized TechnicianDTO createTechnician(TechnicianRequest request) {
        InputValidator.required(request, "Technician details");
        User admin = userService.requireAdmin(request.adminId());
        User user = userService.getUser(request.userId());
        String specialization = InputValidator.required(request.specialization(), "Specialization", 100);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("An administrator account cannot be made a technician.");
        }

        Technician technician = technicianRepository.findByUserId(user.getId()).orElse(null);

        if (technician != null && user.getRole() == Role.TECHNICIAN) {
            throw new IllegalArgumentException(user.getFullName() + " is already a technician.");
        }

        if (technician == null) {
            technician = new Technician(user, specialization);
        } else {
            technician.setSpecialization(specialization);
            technician.setAvailable(true);
        }

        user.setRole(Role.TECHNICIAN);
        userRepository.save(user);
        technicianRepository.save(technician);

        logger.info("Technician created: " + user.getUsername() + " - " + specialization
                + " (by " + admin.getUsername() + ")");
        return mapper.toTechnicianDTO(technician, 0);
    }

    public List<TechnicianDTO> getAllTechnicians() {
        List<TechnicianDTO> result = new ArrayList<>();
        for (Technician technician : technicianRepository.findAll()) {
            result.add(mapper.toTechnicianDTO(technician, countOpenTickets(technician.getId())));
        }
        return result;
    }

    public TechnicianDTO getTechnicianById(Long id) {
        Technician technician = getTechnician(id);
        return mapper.toTechnicianDTO(technician, countOpenTickets(id));
    }

    public TechnicianDTO updateTechnician(Long id, TechnicianUpdateRequest request) {
        InputValidator.required(request, "Technician details");
        User admin = userService.requireAdmin(request.adminId());
        Technician technician = getTechnician(id);

        technician.setSpecialization(InputValidator.required(request.specialization(), "Specialization", 100));
        technician.setAvailable(request.available());
        technicianRepository.save(technician);

        logger.info("Technician updated: " + technician.getUser().getUsername()
                + " available=" + request.available() + " (by " + admin.getUsername() + ")");
        return mapper.toTechnicianDTO(technician, countOpenTickets(id));
    }

    // tickets assigned to a technician
    public List<TicketDTO> getAssignedTickets(Long technicianId) {
        getTechnician(technicianId);  // checks the technician exists

        List<TicketDTO> result = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findByTechnician(technicianId)) {
            result.add(mapper.toTicketDTO(ticket));
        }
        return result;
    }

    // get technician or throw
    public Technician getTechnician(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Technician ID is required.");
        }
        return technicianRepository.findById(id)
                .orElseThrow(() -> new TechnicianNotFoundException("Technician " + id + " was not found."));
    }

    public int countOpenTickets(Long technicianId) {
        int count = 0;
        for (Ticket ticket : ticketRepository.findByTechnician(technicianId)) {
            if (ticket.getStatus().isOpen()) {
                count++;
            }
        }
        return count;
    }
}
