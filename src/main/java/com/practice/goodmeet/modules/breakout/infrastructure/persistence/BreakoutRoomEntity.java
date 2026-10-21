package com.practice.goodmeet.modules.breakout.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "breakout_rooms")
class BreakoutRoomEntity {

  @Id private UUID id;

  @Column(name = "meeting_id", nullable = false)
  private UUID meetingId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "media_room_id", nullable = false, length = 100)
  private String mediaRoomId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "closed_at")
  private Instant closedAt;

  protected BreakoutRoomEntity() {}

  BreakoutRoomEntity(
      UUID id,
      UUID meetingId,
      UUID sessionId,
      String name,
      String mediaRoomId,
      Instant createdAt,
      Instant closedAt) {
    this.id = id;
    this.meetingId = meetingId;
    this.sessionId = sessionId;
    this.name = name;
    this.mediaRoomId = mediaRoomId;
    this.createdAt = createdAt;
    this.closedAt = closedAt;
  }

  UUID getId() {
    return id;
  }

  UUID getMeetingId() {
    return meetingId;
  }

  UUID getSessionId() {
    return sessionId;
  }

  String getName() {
    return name;
  }

  String getMediaRoomId() {
    return mediaRoomId;
  }

  Instant getCreatedAt() {
    return createdAt;
  }

  Instant getClosedAt() {
    return closedAt;
  }
}
