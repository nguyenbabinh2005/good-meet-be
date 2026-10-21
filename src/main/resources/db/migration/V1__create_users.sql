CREATE TABLE users
(
    id            UUID         NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ux_users_email ON users (lower(email));
