# University Help Desk & Ticketing System

CIT3009 Advanced Programming group project. Students and staff submit IT support tickets, technicians work on them, and administrators assign and report on them. Users get real-time notifications when something happens to a ticket.

```
JavaFX Client --(REST/JSON, port 8080)--> Spring Boot backend --(JPA)--> MySQL
      ^                                         |
      | notifications (TCP, port 5000)          | sends notifications (TCP, port 5000)
      |                                         v
      +--------------------------------- Notification Server
```

## Projects

| Folder | What it is | Member |
|---|---|---|
| [`helpdesk-client`](helpdesk-client) | JavaFX desktop client: login, user, technician and admin dashboards | Member 1 – Front-End |
| [`helpdesk-service`](helpdesk-service) | Spring Boot REST API: controllers, services, DTOs | Member 2 – Backend |
| [`helpdesk-service/database`](helpdesk-service/database) | MySQL script; JPA entities (`model`) and `@Repository` classes (`repository`) inside `helpdesk-service` | Member 3 – Data & Persistence |
| [`notification-server`](notification-server) | TCP notification server, plus the notification and Log4j2 parts of `helpdesk-service` | Member 4 – Systems |
| [`docs`](docs) | Guided labs for Member 4's parts | Member 4 |

Each project is a separate Maven project. In Eclipse: **File → Import → Maven → Existing Maven Projects**, then select the project folder.

Requires **JDK 21** or later.

## How to run

Start them in this order:

1. **MySQL** (the same server as Guided Lab 08). Run [`helpdesk-service/database/helpdesk.sql`](helpdesk-service/database/helpdesk.sql) once in phpMyAdmin or MySQL Workbench. It creates `helpdesk_db`, the 4 tables and the admin account (**admin / Admin@123**). If your MySQL isn't on port 3307 with `root` / `usbw`, change the 3 `spring.datasource` lines in `helpdesk-service/src/main/resources/application.properties`.
2. **Notification Server**: run `notification-server/.../server/NotificationServer.java`. It listens on port **5000**.
3. **Backend**: run `helpdesk-service/.../HelpDeskApplication.java`. The REST API is on port **8080**.
4. **Client**: run `helpdesk-client/.../Launcher.java`, then log in.

## Notifications (Member 4)

| Event | Who is notified |
|---|---|
| Ticket Assigned | ticket creator + assigned technician |
| Ticket Resolved | ticket creator |
| New Comment Added | ticket creator + assigned technician |
| Urgent Ticket Created | every ADMIN and TECHNICIAN online |

- **Notification Server.** `ClientHandler` is one thread per connected client (Lab 09). `SessionManager` keeps the connected sessions. `BroadcastTask` delivers each notification on a virtual thread (Lab 10).
- **Backend.** `SocketNotificationService` (`@Component`) implements the backend's `NotificationService` interface. `SendNotificationTask` sends each notification in the background.
- **Shared classes.** `Notification`, `Request` and `Response` (`jm.edu.utech.helpdesk.notification.model`) must be identical in every project that talks to the server.
- **Testing without the GUI.** Run `notification-server/.../test/TestClient.java`.

## Logging (Log4j2)

Each program writes an application log and an error log (ERROR and FATAL only) to its own `logs` folder. Both logs roll over daily or when they reach their size limit.

| Program | Application log | Error log |
|---|---|---|
| Notification Server | `notification-server.log` | `notification-server-error.log` |
| Backend | `helpdesk-backend.log` | `helpdesk-backend-error.log` |

## Integration status

- [x] Notification Server: built and tested.
- [x] Backend notifications and Log4j2: `SocketNotificationService`, `SendNotificationTask`, `log4j2.xml` added to `helpdesk-service`.
- [x] Database layer: JPA entities (User, Technician, Ticket, Comment), `@Repository` classes, MySQL script and settings. Tested end to end with a MySQL-compatible database.
- [x] Client notifications: `helpdesk-client`'s `NotificationListener` uses the shared `Request` / `Response` objects on port 5000.
