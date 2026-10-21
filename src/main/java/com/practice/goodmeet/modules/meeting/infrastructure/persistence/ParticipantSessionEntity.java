package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participant_sessions")
class ParticipantSessionEntity {

  @Id private UUID id;

  @Column(name = "participant_id", nullable = false)
  private UUID participantId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @Column(name = "left_at")
  private Instant leftAt;

  protected ParticipantSessionEntity() {}

  ParticipantSessionEntity(
      UUID id, UUID participantId, UUID sessionId, Instant joinedAt, Instant leftAt) {
    this.id = id;
    this.participantId = participantId;
    this.sessionId = sessionId;
    this.joinedAt = joinedAt;
    this.leftAt = leftAt;
  }

  UUID getId() {
    return id;
  }

  UUID getParticipantId() {
    return participantId;
  }

  UUID getSessionId() {
    return sessionId;
  }

  Instant getJoinedAt() {
    return joinedAt;
  }

  Instant getLeftAt() {
    return leftAt;
  }
}
