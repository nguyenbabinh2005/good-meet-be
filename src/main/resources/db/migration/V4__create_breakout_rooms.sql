CREATE TABLE breakout_rooms
(
    id            UUID         NOT NULL,
    meeting_id    UUID         NOT NULL,
    session_id    UUID         NOT NULL,
    name          VARCHAR(100) NOT NULL,
    media_room_id VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at     TIMESTAMPTZ,
    CONSTRAINT pk_breakout_rooms PRIMARY KEY (id),
    CONSTRAINT fk_breakout_rooms_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id),
    CONSTRAINT fk_breakout_rooms_session FOREIGN KEY (session_id) REFERENCES meeting_sessions (id),
    CONSTRAINT uq_breakout_rooms_media_room_id UNIQUE (media_room_id)
);
