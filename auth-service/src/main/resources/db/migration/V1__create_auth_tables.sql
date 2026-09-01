-- A.2.1 -- auth-service schema.
-- Everything the service needs to identify a user and manage refresh-token families.

CREATE TABLE app_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(72)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    -- Enforced in the DB, not just in the service. Two concurrent registrations
    -- with the same username both pass an application-level "does it exist?"
    -- check; only the database can actually stop the second one.
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT uk_app_user_email    UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE role (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE = InnoDB;

CREATE TABLE refresh_token (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    -- SHA-256 hex of the opaque token. Never the token itself (A.5.3).
    token_hash     CHAR(64)    NOT NULL,
    -- All tokens descended from one login share a family_id. Reuse detection
    -- revokes the whole family, so this column needs an index (A.5.5).
    family_id      CHAR(36)    NOT NULL,
    issued_at      DATETIME(6) NOT NULL,
    expires_at     DATETIME(6) NOT NULL,
    revoked_at     DATETIME(6) NULL,
    replaced_by_id BIGINT      NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash     UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user     FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_token_replaced FOREIGN KEY (replaced_by_id) REFERENCES refresh_token (id)
) ENGINE = InnoDB;

CREATE INDEX idx_refresh_token_family ON refresh_token (family_id);
CREATE INDEX idx_refresh_token_user   ON refresh_token (user_id);
