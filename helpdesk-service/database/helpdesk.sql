-- member 3 (Data & Persistence)
-- University Help Desk database. Run this once in phpMyAdmin or MySQL Workbench
-- before starting the backend (Guided Lab 08, Activity 1).

CREATE DATABASE IF NOT EXISTS helpdesk_db;

USE helpdesk_db;

-- every account: students/staff (USER), technicians and administrators
CREATE TABLE IF NOT EXISTS users (
    user_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    role           VARCHAR(20)  NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME(6)  NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'TECHNICIAN', 'ADMIN'))
);

-- one row per technician, linked one-to-one to their user account
CREATE TABLE IF NOT EXISTS technicians (
    technician_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL UNIQUE,
    specialization  VARCHAR(100),
    available       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_technician_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- one user has many tickets; one technician has many tickets
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject           VARCHAR(150)  NOT NULL,
    description       VARCHAR(2000) NOT NULL,
    creation_date     DATETIME(6)   NOT NULL,
    priority          VARCHAR(20)   NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    created_by        BIGINT        NOT NULL,
    technician_id     BIGINT,
    resolution_notes  VARCHAR(2000),
    resolved_date     DATETIME(6),
    closed_date       DATETIME(6),
    last_updated      DATETIME(6)   NOT NULL,
    CONSTRAINT fk_ticket_user FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT fk_ticket_technician FOREIGN KEY (technician_id) REFERENCES technicians (technician_id),
    CONSTRAINT chk_ticket_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_ticket_status CHECK (status IN ('NEW', 'ASSIGNED', 'IN_PROGRESS', 'PENDING',
                                                   'RESOLVED', 'CLOSED', 'REOPENED'))
);

-- one ticket has many comments
CREATE TABLE IF NOT EXISTS comments (
    comment_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id     BIGINT        NOT NULL,
    author_id     BIGINT        NOT NULL,
    comment_text  VARCHAR(2000) NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users (user_id)
);

-- default administrator account
-- username: admin    password: Admin@123   (hash made with PasswordHasher - change it after the demo)
INSERT INTO users (username, password_hash, full_name, email, role, active, created_at)
SELECT 'admin', 'PVM+/ZlSMApX6BELdBQ4RQ==:KJwOcCQ3keVecrPnkiLNZjuwOhgh8W/sc7Vd35IkggI=',
       'System Administrator', 'admin@utech.edu.jm', 'ADMIN', TRUE, NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

DESCRIBE users;
DESCRIBE technicians;
DESCRIBE tickets;
DESCRIBE comments;
