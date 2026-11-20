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

import jm.edu.utech.helpdesk.dto.TechnicianDTO;
import jm.edu.utech.helpdesk.dto.TechnicianRequest;
import jm.edu.utech.helpdesk.dto.TechnicianUpdateRequest;
import jm.edu.utech.helpdesk.dto.TicketDTO;
import jm.edu.utech.helpdesk.service.TechnicianService;

// technician endpoints
@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

    private final TechnicianService technicianService;

    @Autowired
    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    // POST /api/technicians  (admin)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicianDTO createTechnician(@RequestBody TechnicianRequest request) {
        return technicianService.createTechnician(request);
    }

    // GET /api/technicians
    @GetMapping
    public List<TechnicianDTO> getAllTechnicians() {
        return technicianService.getAllTechnicians();
    }

    // GET /api/technicians/{id}
    @GetMapping("/{id}")
    public TechnicianDTO getTechnician(@PathVariable("id") Long id) {
        return technicianService.getTechnicianById(id);
    }

    // PUT /api/technicians/{id}  (admin)
    @PutMapping("/{id}")
    public TechnicianDTO updateTechnician(@PathVariable("id") Long id, @RequestBody TechnicianUpdateRequest request) {
        return technicianService.updateTechnician(id, request);
    }

    // GET /api/technicians/{id}/tickets  (technician dashboard)
    @GetMapping("/{id}/tickets")
    public List<TicketDTO> getAssignedTickets(@PathVariable("id") Long id) {
        return technicianService.getAssignedTickets(id);
    }
}
