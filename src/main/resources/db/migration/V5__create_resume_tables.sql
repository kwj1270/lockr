-- Resume table for storing user application resumes to teams
CREATE TABLE `resumes`
(
    id                      VARCHAR(255) NOT NULL PRIMARY KEY,
    team_id                 VARCHAR(255) NOT NULL,
    user_id                 VARCHAR(255) NOT NULL,
    birth                   VARCHAR(255) NOT NULL,
    weight                  VARCHAR(255) NOT NULL,
    height                  VARCHAR(255) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    email                   VARCHAR(255) NOT NULL,
    address                 VARCHAR(255) NOT NULL,
    phone                   VARCHAR(255) NOT NULL,
    emergency_contact_phone VARCHAR(255) NOT NULL,
    nationality             VARCHAR(255) NOT NULL,
    preferred_position      VARCHAR(255) NOT NULL,
    foot                    VARCHAR(10)  NOT NULL CHECK (foot IN ('LEFT', 'RIGHT', 'BOTH')),
    advantages              TEXT,
    disadvantages           TEXT,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP    NULL
) COLLATE = utf8mb4_unicode_ci;
-- Indexes for better query performance
CREATE INDEX idx_resume_team_id ON `resumes` (team_id);
CREATE INDEX idx_resume_user_id ON `resumes` (user_id);
CREATE INDEX idx_resume_created_at ON `resumes` (created_at);

-- Contract table for storing signed contracts between users and teams
CREATE TABLE `contracts`
(
    id                     VARCHAR(255) NOT NULL PRIMARY KEY,
    team_id                VARCHAR(255) NOT NULL,
    individual_user_id     VARCHAR(255) NOT NULL,
    representative_user_id VARCHAR(255) NOT NULL,
    representative_role    VARCHAR(255) NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at             TIMESTAMP    NULL
) COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_contract_team_id ON `contracts` (team_id);
CREATE INDEX idx_contract_individual_user_id ON `contracts` (individual_user_id);
CREATE INDEX idx_contract_representative_user_id ON `contracts` (representative_user_id);
CREATE INDEX idx_contract_created_at ON `contracts` (created_at);
