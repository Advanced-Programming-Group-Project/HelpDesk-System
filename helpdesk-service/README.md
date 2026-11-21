# Help Desk – Member 2 (Backend)
created by rickayla mcneil 2111687

REST controllers, service layer and DTOs (Spring Boot, port 8080).

## Added by other members
- **Member 3 (database):** JPA annotations on the `model` classes, a `@Repository` class for each of the 4 interfaces in `repository` (`JpaUserRepository`, `JpaTechnicianRepository`, `JpaTicketRepository`, `JpaCommentRepository`), Hibernate and MySQL in the pom, database settings in `application.properties`, and `database/helpdesk.sql` (tables + default ADMIN account: admin / Admin@123).
- **Member 4:** `SocketNotificationService` (`@Component` for `NotificationService`), `SendNotificationTask`, and `log4j2.xml`.

Before running: run `database/helpdesk.sql` in MySQL, then check the `spring.datasource` lines in `application.properties`.

