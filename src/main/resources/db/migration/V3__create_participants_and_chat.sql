CREATE TABLE participants
(
    id             UUID         NOT NULL,
    meeting_id     UUID         NOT NULL,
    user_id        UUID,
    display_name   VARCHAR(100) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    media_identity VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_participants PRIMARY KEY (id),
    CONSTRAINT fk_participants_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id),
    CONSTRAINT fk_participants_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_participants_meeting_user UNIQUE (meeting_id, user_id),
    CONSTRAINT uq_participants_meeting_media_identity UNIQUE (meeting_id, media_identity),
    CONSTRAINT ck_participants_role CHECK (role IN ('HOST', 'PARTICIPANT', 'GUEST')),
    CONSTRAINT ck_participants_guest_role CHECK ((user_id IS NULL) = (role = 'GUEST'))
);

CREATE UNIQUE INDEX ux_participants_host ON participants (meeting_id) WHERE role = 'HOST';

CREATE TABLE participant_sessions
(
    id             UUID        NOT NULL,
    participant_id UUID        NOT NULL,
    session_id     UUID        NOT NULL,
    joined_at      TIMESTAMPTZ NOT NULL,
    left_at        TIMESTAMPTZ,
    CONSTRAINT pk_participant_sessions PRIMARY KEY (id),
    CONSTRAINT fk_participant_sessions_participant FOREIGN KEY (participant_id) REFERENCES participants (id),
    CONSTRAINT fk_participant_sessions_session FOREIGN KEY (session_id) REFERENCES meeting_sessions (id),
    CONSTRAINT ck_participant_sessions_left CHECK (left_at IS NULL OR left_at >= joined_at)
);



CREATE UNIQUE INDEX ux_participant_sessions_open
    ON participant_sessions (participant_id, session_id) WHERE left_at IS NULL;

CREATE TABLE chat_messages
(
    id                    UUID          NOT NULL,
    meeting_id            UUID          NOT NULL,
    session_id            UUID          NOT NULL,
    sender_participant_id UUID          NOT NULL,
    content               VARCHAR(2000) NOT NULL,
    sent_at               TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_chat_messages PRIMARY KEY (id),
    CONSTRAINT fk_chat_messages_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES meeting_sessions (id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_participant_id) REFERENCES participants (id)

);

