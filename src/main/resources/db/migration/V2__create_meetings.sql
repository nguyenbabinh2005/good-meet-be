CREATE TABLE meetings
(
    id                 UUID         NOT NULL,
    public_code        VARCHAR(20)  NOT NULL,
    title              VARCHAR(200) NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    scheduled_start_at TIMESTAMPTZ,
    scheduled_end_at   TIMESTAMPTZ,
    media_room_id      VARCHAR(100) NOT NULL,
    captions_enabled   BOOLEAN      NOT NULL DEFAULT FALSE,
    notes_enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    assistant_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_meetings PRIMARY KEY (id),
    CONSTRAINT uq_meetings_public_code UNIQUE (public_code),
    CONSTRAINT uq_meetings_media_room_id UNIQUE (media_room_id),
    CONSTRAINT ck_meetings_status CHECK (status IN ('SCHEDULED', 'ACTIVE', 'ENDED', 'CANCELLED')),
    CONSTRAINT ck_meetings_schedule CHECK (
        (scheduled_start_at IS NULL AND scheduled_end_at IS NULL)
        OR (scheduled_start_at IS NOT NULL
            AND scheduled_end_at IS NOT NULL
            AND scheduled_end_at > scheduled_start_at)
    )
);

CREATE TABLE meeting_invitations
(
    id         UUID         NOT NULL,
    meeting_id UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_meeting_invitations PRIMARY KEY (id),
    CONSTRAINT fk_meeting_invitations_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE,
    CONSTRAINT uq_meeting_invitations_meeting_email UNIQUE (meeting_id, email),
    CONSTRAINT ck_meeting_invitations_email_lower CHECK (email = lower(email))
);

CREATE TABLE meeting_sessions
(
    id         UUID        NOT NULL,
    meeting_id UUID        NOT NULL,
    status     VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at   TIMESTAMPTZ,
    CONSTRAINT pk_meeting_sessions PRIMARY KEY (id),
    CONSTRAINT fk_meeting_sessions_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id),
    CONSTRAINT ck_meeting_sessions_status CHECK (status IN ('ACTIVE', 'ENDED')),
    CONSTRAINT ck_meeting_sessions_ended CHECK ((status = 'ENDED') = (ended_at IS NOT NULL))
);


CREATE UNIQUE INDEX ux_meeting_sessions_active ON meeting_sessions (meeting_id) WHERE status = 'ACTIVE';
