# University Help Desk & Ticketing System

CIT3009 Advanced Programming group project. Students and staff submit IT support tickets, technicians work on them, and administrators assign tickets and generate reports. Users get real-time notifications when something happens to a ticket.

```
JavaFX Client --(REST/JSON, port 8080)--> Spring Boot backend --(JPA/Hibernate)--> MySQL
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
| [`helpdesk-service`](helpdesk-service) (`model`, `repository`, `database`) | MySQL database, JPA entities and repositories | Member 3 – Data & Persistence |
| [`notification-server`](notification-server) | TCP notification server, plus notifications and Log4j2 in `helpdesk-service` | Member 4 – Systems |

Each folder is a separate Maven project. In Eclipse: **File → Import → Maven → Existing Maven Projects**, then select the folder.

Requires **JDK 21** or later and **MySQL**.

## How to run

Start them in this order:

1. **MySQL.** Run [`helpdesk-service/database/helpdesk.sql`](helpdesk-service/database/helpdesk.sql) once in phpMyAdmin or MySQL Workbench. It creates the `helpdesk_db` database, its tables, and the admin account. The backend connects on port 3307 as `root` / `usbw`. If your MySQL is different, change the `spring.datasource` lines in `helpdesk-service/src/main/resources/application.properties`.
2. **Notification Server.** Run `notification-server/.../server/NotificationServer.java`. It listens on port **5000**.
3. **Backend.** Run `helpdesk-service/.../HelpDeskApplication.java`. The REST API is on port **8080**.
4. **Client.** Run `helpdesk-client/.../Launcher.java` and log in.

**Default admin login:** `admin` / `Admin@123`

## Database

| Table | Relationships |
|---|---|
| `users` | one user → many tickets |
| `technicians` | linked one-to-one to a user; one technician → many tickets |
| `tickets` | one ticket → many comments |
| `comments` | belongs to one ticket and one author |

## Notifications

| Event | Who is notified |
|---|---|
| Ticket Assigned | ticket creator + assigned technician |
| Ticket Resolved | ticket creator |
| New Comment Added | ticket creator + assigned technician |
| Urgent Ticket Created | every admin and technician online |

The Notification Server runs one `ClientHandler` thread per connected client and delivers each notification on a virtual thread. The backend sends notifications on a background thread, so ticket requests never wait for the server.

## Logging (Log4j2)

Each program writes an application log and an error log (ERROR and FATAL only) to its own `logs` folder. Both logs roll over daily or when they reach 10 MB.

| Program | Application log | Error log |
|---|---|---|
| Notification Server | `notification-server.log` | `notification-server-error.log` |
| Backend | `helpdesk-backend.log` | `helpdesk-backend-error.log` |
