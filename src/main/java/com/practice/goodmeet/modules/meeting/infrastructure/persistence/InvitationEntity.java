package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meeting_invitations")
class InvitationEntity {

  @Id private UUID id;

  @Column(name = "meeting_id", nullable = false)
  private UUID meetingId;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected InvitationEntity() {}

  InvitationEntity(UUID id, UUID meetingId, String email, Instant createdAt) {
    this.id = id;
    this.meetingId = meetingId;
    this.email = email;
    this.createdAt = createdAt;
  }

  UUID getId() {
    return id;
  }

  UUID getMeetingId() {
    return meetingId;
  }

  String getEmail() {
    return email;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
