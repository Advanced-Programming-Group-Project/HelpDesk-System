package jm.edu.utech.helpdesk.client.api;
// Member 1 (Front-End)

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jm.edu.utech.helpdesk.client.model.AverageResolutionTimeDTO;
import jm.edu.utech.helpdesk.client.model.CommentDTO;
import jm.edu.utech.helpdesk.client.model.ErrorResponse;
import jm.edu.utech.helpdesk.client.model.MessageResponse;
import jm.edu.utech.helpdesk.client.model.Priority;
import jm.edu.utech.helpdesk.client.model.Role;
import jm.edu.utech.helpdesk.client.model.TechnicianDTO;
import jm.edu.utech.helpdesk.client.model.TechnicianWorkloadDTO;
import jm.edu.utech.helpdesk.client.model.TicketCountReport;
import jm.edu.utech.helpdesk.client.model.TicketDTO;
import jm.edu.utech.helpdesk.client.model.TicketStatus;
import jm.edu.utech.helpdesk.client.model.UserDTO;
import jm.edu.utech.helpdesk.client.util.AppConfig;

// talks to the Spring Boot REST service using JSON over HTTP.
// every method blocks, so controllers call them from a background Task (never the FX thread).
public class HelpDeskApi {

    private static final HelpDeskApi INSTANCE = new HelpDeskApi(AppConfig.apiBaseUrl());

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper json;

    public HelpDeskApi(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static HelpDeskApi get() {
        return INSTANCE;
    }

    public ObjectMapper mapper() {
        return json;
    }

    // ---------------- users ----------------

    public UserDTO login(String username, String password) {
        return post("/users/login", body("username", username, "password", password), UserDTO.class);
    }

    public MessageResponse logout(Long userId) {
        return post("/users/logout", body("userId", userId), MessageResponse.class);
    }

    public UserDTO register(String username, String password, String fullName, String email) {
        return post("/users/register",
                body("username", username, "password", password, "fullName", fullName, "email", email),
                UserDTO.class);
    }

    public List<UserDTO> getAllUsers() {
        return getList("/users", new TypeReference<>() { });
    }

    public UserDTO updateUser(Long id, String fullName, String email) {
        return put("/users/" + id, body("fullName", fullName, "email", email), UserDTO.class);
    }

    public MessageResponse changePassword(Long id, String currentPassword, String newPassword) {
        return put("/users/" + id + "/password",
                body("currentPassword", currentPassword, "newPassword", newPassword), MessageResponse.class);
    }

    public UserDTO changeRole(Long id, Long adminId, Role role) {
        return put("/users/" + id + "/role", body("adminId", adminId, "role", role), UserDTO.class);
    }

    public UserDTO setAccountStatus(Long id, Long adminId, boolean active) {
        return put("/users/" + id + "/status", body("adminId", adminId, "active", active), UserDTO.class);
    }

    // ---------------- tickets ----------------

    public TicketDTO createTicket(Long userId, String subject, String description, Priority priority) {
        return post("/tickets",
                body("userId", userId, "subject", subject, "description", description, "priority", priority),
                TicketDTO.class);
    }

    public List<TicketDTO> getAllTickets() {
        return getList("/tickets", new TypeReference<>() { });
    }

    public List<TicketDTO> getTicketsByUser(Long userId) {
        return getList("/tickets/user/" + userId, new TypeReference<>() { });
    }

    public TicketDTO getTicket(Long id) {
        return send(request("/tickets/" + id).GET().build(), TicketDTO.class);
    }

    public List<TicketDTO> searchTickets(Long ticketId, Long userId, Long technicianId, TicketStatus status,
                                         Priority priority, LocalDate from, LocalDate to) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ticketId", ticketId);
        params.put("userId", userId);
        params.put("technicianId", technicianId);
        params.put("status", status);
        params.put("priority", priority);
        params.put("from", from);
        params.put("to", to);
        return getList("/tickets/search" + query(params), new TypeReference<>() { });
    }

    public TicketDTO updateTicket(Long id, Long userId, String subject, String description, Priority priority) {
        return put("/tickets/" + id,
                body("userId", userId, "subject", subject, "description", description, "priority", priority),
                TicketDTO.class);
    }

    public TicketDTO assignTicket(Long id, Long adminId, Long technicianId, boolean reassign) {
        return put("/tickets/" + id + "/assign",
                body("adminId", adminId, "technicianId", technicianId, "reassign", reassign), TicketDTO.class);
    }

    public TicketDTO updateStatus(Long id, Long technicianId, TicketStatus status) {
        return put("/tickets/" + id + "/status",
                body("technicianId", technicianId, "status", status), TicketDTO.class);
    }

    public TicketDTO resolveTicket(Long id, Long technicianId, String notes) {
        return put("/tickets/" + id + "/resolve",
                body("technicianId", technicianId, "resolutionNotes", notes), TicketDTO.class);
    }

    public TicketDTO closeTicket(Long id, Long userId, String reason) {
        return put("/tickets/" + id + "/close", body("userId", userId, "reason", reason), TicketDTO.class);
    }

    public TicketDTO reopenTicket(Long id, Long userId, String reason) {
        return put("/tickets/" + id + "/reopen", body("userId", userId, "reason", reason), TicketDTO.class);
    }

    public CommentDTO addComment(Long ticketId, Long authorId, String text) {
        return post("/tickets/" + ticketId + "/comments", body("authorId", authorId, "text", text),
                CommentDTO.class);
    }

    public List<CommentDTO> getComments(Long ticketId) {
        return getList("/tickets/" + ticketId + "/comments", new TypeReference<>() { });
    }

    // ---------------- technicians ----------------

    public List<TechnicianDTO> getAllTechnicians() {
        return getList("/technicians", new TypeReference<>() { });
    }

    public TechnicianDTO createTechnician(Long adminId, Long userId, String specialization) {
        return post("/technicians",
                body("adminId", adminId, "userId", userId, "specialization", specialization), TechnicianDTO.class);
    }

    public TechnicianDTO updateTechnician(Long id, Long adminId, String specialization, boolean available) {
        return put("/technicians/" + id,
                body("adminId", adminId, "specialization", specialization, "available", available),
                TechnicianDTO.class);
    }

    public List<TicketDTO> getAssignedTickets(Long technicianId) {
        return getList("/technicians/" + technicianId + "/tickets", new TypeReference<>() { });
    }

    // the login response is a UserDTO; a technician's technician-ID is looked up by user ID
    public Long findTechnicianIdForUser(Long userId) {
        for (TechnicianDTO technician : getAllTechnicians()) {
            if (userId.equals(technician.userId())) {
                return technician.id();
            }
        }
        return null;
    }

    // ---------------- reports ----------------

    public TicketCountReport ticketsSubmitted(LocalDate from, LocalDate to) {
        return send(request("/reports/tickets-submitted" + dateRange(from, to)).GET().build(),
                TicketCountReport.class);
    }

    public TicketCountReport ticketsResolved(LocalDate from, LocalDate to) {
        return send(request("/reports/tickets-resolved" + dateRange(from, to)).GET().build(),
                TicketCountReport.class);
    }

    public TicketCountReport openTickets() {
        return send(request("/reports/open-tickets").GET().build(), TicketCountReport.class);
    }

    public List<TechnicianWorkloadDTO> technicianWorkload() {
        return getList("/reports/technician-workload", new TypeReference<>() { });
    }

    public AverageResolutionTimeDTO averageResolutionTime(LocalDate from, LocalDate to) {
        return send(request("/reports/average-resolution-time" + dateRange(from, to)).GET().build(),
                AverageResolutionTimeDTO.class);
    }

    // ---------------- HTTP helpers ----------------

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
    }

    private <T> T post(String path, Map<String, Object> body, Class<T> type) {
        return send(request(path).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body))).build(), type);
    }

    private <T> T put(String path, Map<String, Object> body, Class<T> type) {
        return send(request(path).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(toJson(body))).build(), type);
    }

    private <T> List<T> getList(String path, TypeReference<List<T>> type) {
        String text = execute(request(path).GET().build());
        try {
            return json.readValue(text, type);
        } catch (IOException e) {
            throw new ApiException(500, "The server sent data the client could not read.", e);
        }
    }

    private <T> T send(HttpRequest request, Class<T> type) {
        String text = execute(request);
        try {
            return json.readValue(text, type);
        } catch (IOException e) {
            throw new ApiException(500, "The server sent data the client could not read.", e);
        }
    }

    // runs the request; turns HTTP errors and network failures into ApiException
    private String execute(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new ApiException(ApiException.NO_CONNECTION,
                    "Could not reach the Help Desk server at " + baseUrl
                            + ". Make sure the server is running and try again.", e);
        } catch (HttpTimeoutException e) {
            throw new ApiException(ApiException.NO_CONNECTION, "The server took too long to respond.", e);
        } catch (IOException e) {
            throw new ApiException(ApiException.NO_CONNECTION,
                    "Network error while contacting the server: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiException.NO_CONNECTION, "The request was interrupted.", e);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response.body();
        }
        throw new ApiException(status, readErrorMessage(response));
    }

    // uses the "message" from the server's ErrorResponse when there is one
    private String readErrorMessage(HttpResponse<String> response) {
        String text = response.body();
        if (text != null && !text.isBlank()) {
            try {
                ErrorResponse error = json.readValue(text, ErrorResponse.class);
                if (error.message() != null && !error.message().isBlank()) {
                    return error.message();
                }
            } catch (IOException ignored) {
                // not an ErrorResponse - fall through to the generic message
            }
        }
        return "The server returned an error (HTTP " + response.statusCode() + ").";
    }

    private String toJson(Map<String, Object> body) {
        try {
            return json.writeValueAsString(body);
        } catch (IOException e) {
            throw new ApiException(400, "Could not prepare the request data.", e);
        }
    }

    // body("a", 1, "b", 2) -> {"a":1,"b":2}; allows null values (Map.of does not)
    private static Map<String, Object> body(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static String dateRange(LocalDate from, LocalDate to) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("from", from);
        params.put("to", to);
        return query(params);
    }

    // builds ?a=1&b=2 skipping nulls
    static String query(Map<String, Object> params) {
        StringJoiner joiner = new StringJoiner("&", "?", "");
        joiner.setEmptyValue("");
        params.forEach((key, value) -> {
            if (value != null) {
                joiner.add(key + "=" + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
            }
        });
        return joiner.toString();
    }
}
