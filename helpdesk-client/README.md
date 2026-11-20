# Help Desk – Member 1 (Front-End)

JavaFX desktop client for the University Help Desk & Ticketing System.
It talks to Member 2's Spring Boot REST service (JSON over HTTP) and listens to Member 4's TCP notification server.

## Run it

```bash
cd helpdesk-client
mvn javafx:run
```

From an IDE, run `jm.edu.utech.helpdesk.client.Launcher`. This avoids the "JavaFX runtime components are missing" error.

Requires JDK 21 or newer. JDK 23 is fine.

## Settings: `src/main/resources/client.properties`

| key | default | what |
|---|---|---|
| `api.baseUrl` | `http://localhost:8080/api` | Spring Boot service (Member 2) |
| `notification.host` / `notification.port` | `localhost` / `5050` | TCP notification server (Member 4) |
| `refresh.seconds` | `15` | technician dashboard auto-refresh |

To change one, edit `client.properties`. When running `Launcher` from an IDE, you can instead add a VM argument such as `-Dapi.baseUrl=http://192.168.0.10:8080/api`. (`mvn javafx:run -D...` does **not** pass the value to the app.)

## Screens

| Screen | Who | What it does |
|---|---|---|
| Login + Register | everyone | Validates input, then opens the right dashboard for the user's role |
| User Dashboard | USER | Submit, edit, close and reopen tickets; view status; comments |
| Technician Dashboard | TECHNICIAN | Assigned tickets with background auto-refresh; set IN_PROGRESS/PENDING; resolve with notes |
| Administrator Dashboard | ADMIN | All tickets; search by ID, user, technician, status, priority and date range; assign/reassign; manage users, roles, technicians; reports and charts |
| My Account | everyone | Update name and email; change password |
| Ticket Details | everyone | Full ticket, comment thread, add comment |

## Where each marked requirement is

- **Stage / Scene / Navigation:** `Navigator` (one Stage, a new Scene per screen) and `HelpDeskClientApp`
- **Layout panes:** BorderPane, HBox, VBox, GridPane, FlowPane, StackPane and TabPane in the `view` package
- **MVC:** `model` holds the DTOs and Session; `view` builds the layouts only; `controller` handles the events and REST calls
- **TableView:** `TicketTable` plus the users, technicians and workload tables in `AdminDashboardView`
- **Event handling:** `setOnAction`, selection listeners and double-click rows in each controller
- **Form validation:** `util/FormValidator`, using the same rules as the backend's `InputValidator`; invalid fields get a red border
- **Alerts:** `util/Alerts`, which covers Invalid Login Credentials, Duplicate User Account, Ticket Not Found, Unable to Connect to Server, and assignment conflicts
- **Multithreading:**
  - Every REST call runs on a background `Task` (`util/Background`), so the GUI never freezes.
  - The technician dashboard auto-refreshes with a `ScheduledService` (spec Requirement 1).
  - Notifications are read on their own thread (`notification/NotificationListener`).
- **Concurrent assignment:** when the server returns 409, the admin sees a "Ticket Assignment Conflict" alert and the table reloads.

## Notes for the team

- **Member 4 (notification server).** On connect, the client sends one line: `REGISTER <userId> <role> <username>`. After that it shows every line the server sends in the Notifications panel and reloads the dashboard. If your server uses a different format, the only method to change is `NotificationListener.register()`. If the server is down, the client keeps working and retries every 10 seconds.
- **Technicians.** Login returns a `UserDTO`, so the client finds the technician ID by matching `userId` in `GET /api/technicians`.
- **Member 3.** The client needs a seeded ADMIN account to log in as an administrator. Registration only creates USER accounts.
