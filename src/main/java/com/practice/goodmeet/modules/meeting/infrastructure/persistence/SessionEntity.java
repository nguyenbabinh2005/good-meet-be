package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import com.practice.goodmeet.modules.meeting.domain.MeetingSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meeting_sessions")
class SessionEntity {

  @Id private UUID id;

  @Column(name = "meeting_id", nullable = false)
  private UUID meetingId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private MeetingSession.Status status;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  protected SessionEntity() {}

  SessionEntity(
      UUID id, UUID meetingId, MeetingSession.Status status, Instant startedAt, Instant endedAt) {
    this.id = id;
    this.meetingId = meetingId;
    this.status = status;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
  }

  UUID getId() {
    return id;
  }

  UUID getMeetingId() {
    return meetingId;
  }

  MeetingSession.Status getStatus() {
    return status;
  }

  Instant getStartedAt() {
    return startedAt;
  }

  Instant getEndedAt() {
    return endedAt;
  }
}
