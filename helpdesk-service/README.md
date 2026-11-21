# Help Desk – Member 2 (Backend)
created by rickayla mcneil 2111687

REST controllers, service layer and DTOs (Spring Boot, port 8080).

## Contents
- **REST controllers, services and DTOs** (Member 2): user, ticket, technician and report endpoints under `/api`.
- **Database** (Member 3): JPA entities in `model`, `@Repository` classes in `repository`, and `database/helpdesk.sql`, which creates the tables and the default admin account (admin / Admin@123).
- **Notifications and logging** (Member 4): `SocketNotificationService` and `SendNotificationTask` send ticket events to the Notification Server on port 5000; `log4j2.xml` sets up the application, error and rolling logs.

## Running
1. Run `database/helpdesk.sql` in MySQL.
2. Check the `spring.datasource` lines in `src/main/resources/application.properties` match your MySQL.
3. Start the Notification Server, then run `HelpDeskApplication`.
