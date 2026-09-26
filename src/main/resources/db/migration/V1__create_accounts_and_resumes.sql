CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    password VARCHAR(60) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resumes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    professional_title VARCHAR(120),
    email VARCHAR(254),
    phone VARCHAR(60),
    location VARCHAR(200),
    website VARCHAR(254),
    summary TEXT,
    experience_content TEXT,
    education_content TEXT,
    skills_content TEXT,
    projects_content TEXT,
    certifications_content TEXT,
    additional_content TEXT,
    template_id VARCHAR(20) NOT NULL,
    profile_photo LONGBLOB,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX ix_resumes_owner_updated (owner_id, updated_at),
    CONSTRAINT fk_resumes_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
